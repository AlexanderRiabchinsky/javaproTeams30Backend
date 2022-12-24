package soialNetworkApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import soialNetworkApp.api.request.DialogUserShortListDto;
import soialNetworkApp.api.request.MessageRq;
import soialNetworkApp.api.request.MessageWsRq;
import soialNetworkApp.api.response.*;
import soialNetworkApp.mappers.PersonMapper;
import soialNetworkApp.model.entities.Dialog;
import soialNetworkApp.model.entities.Friendship;
import soialNetworkApp.model.entities.Message;
import soialNetworkApp.model.entities.Person;
import soialNetworkApp.model.enums.FriendshipStatusTypes;
import soialNetworkApp.model.enums.ReadStatusTypes;
import soialNetworkApp.repository.DialogsRepository;
import soialNetworkApp.repository.FriendshipsRepository;
import soialNetworkApp.repository.MessagesRepository;
import soialNetworkApp.repository.PersonsRepository;
import soialNetworkApp.security.jwt.JWTUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogsService {

    private final FriendshipsRepository friendshipsRepository;

    private final DialogsRepository dialogsRepository;

    private final MessagesRepository messagesRepository;

    private final PersonsRepository personsRepository;

    private final PersonMapper personMapper;

    private final SimpMessagingTemplate template;

    private final JWTUtil jwtUtil;


    public CommonRs<ComplexRs> getUnreadMessages() {
        return CommonRs.<ComplexRs>builder()
                .data(ComplexRs.builder()
                        .count((int) messagesRepository.findAllByRecipientAndIsDeletedFalse(findCurrentUser()).stream()
                                .filter(m -> m.getReadStatus().equals(ReadStatusTypes.SENT))
                                .count())
                        .build())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public CommonRs<ComplexRs> setReadMessages(Long dialogId) {
        AtomicReference<Integer> readCount = new AtomicReference<>(0);
        messagesRepository.findAllByDialogIdAndIsDeletedFalse(dialogId).stream()
                .filter(m -> m.getRecipient().equals(findCurrentUser()))
                .filter(m -> m.getReadStatus().equals(ReadStatusTypes.SENT))
                .forEach(m -> {
                    m.setReadStatus(ReadStatusTypes.READ);
                    messagesRepository.save(m);
                    readCount.getAndSet(readCount.get() + 1);
                });
        return CommonRs.<ComplexRs>builder()
                .data(ComplexRs.builder()
                        .count(readCount.get())
                        .build())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public CommonRs<ComplexRs> beginDialog(DialogUserShortListDto dialogUserShortListDto) {
        Person dialogPerson = personsRepository.findPersonById(dialogUserShortListDto.getUserIds().get(0)).orElseThrow();
        Person currentPerson = findCurrentUser();
        Dialog dialog = (dialogsRepository.findDialogByFirstPersonAndSecondPerson(dialogPerson, currentPerson))
                .orElse(dialogsRepository.findDialogByFirstPersonAndSecondPerson(currentPerson, dialogPerson)
                        .orElse(createNewDialog(dialogPerson)));
        dialogsRepository.save(dialog);
        return CommonRs.<ComplexRs>builder()
                .timestamp(System.currentTimeMillis())
                .data(ComplexRs.builder().build())
                .build();
    }

    private Dialog createNewDialog(Person dialogPerson) {
        Dialog dialog = new Dialog();
        dialog.setFirstPerson(findCurrentUser());
        dialog.setSecondPerson(dialogPerson);
        dialog.setLastActiveTime(ZonedDateTime.now());
        return dialog;
    }

    public CommonRs<List<DialogRs>> getAllDialogs() {
        Person me = findCurrentUser();
        List<DialogRs> dialogRsList = createDialogRsList(me);
        dialogRsList = blockDialogs(dialogRsList, me);
        return CommonRs.<List<DialogRs>>builder()
                .total((long) dialogRsList.size())
                .timestamp(System.currentTimeMillis())
                .data(dialogRsList)
                .build();
    }

    public void getMessageFromWs(MessageWsRq messageWsRq) {
        Message message = Message.builder()
                .dialog(dialogsRepository.findById(messageWsRq.getDialogId()).orElseThrow())
                .author(personsRepository.findById(messageWsRq.getAuthorId()).orElseThrow())
                .messageText(messageWsRq.getMessageText())
                .time(messageWsRq.getTime().toLocalDateTime())
                .readStatus(ReadStatusTypes.valueOf(messageWsRq.getReadStatus()))
                .recipient(getRecipientFromDialog(messageWsRq.getAuthorId(), messageWsRq.getDialogId()))
                .isDeleted(false)
                .build();
        messagesRepository.save(message);
        log.info(messageWsRq.getToken());
        template.convertAndSend(String.format("/user/%s/queue/messages", messageWsRq.getDialogId()),
                MessageWsRs.builder()
                        .id(message.getId())
                        .authorId(message.getAuthor().getId())
                        .userId(personsRepository.findPersonByEmail(jwtUtil.extractUserName(messageWsRq.getToken()))
                                .orElseThrow().getId())
                        .messageText(message.getMessageText())
                        .build());
    }

    public CommonRs<List<MessageRs>> getMessages(Long dialogId) {
        List<MessageRs> messagesRs = new ArrayList<>();
        messagesRepository.findAllByDialogIdAndIsDeletedFalse(dialogId)
                .forEach(m -> {
                    m.setReadStatus(ReadStatusTypes.READ);
                    messagesRs.add(createMessageRs(m));
                });
        return CommonRs.<List<MessageRs>>builder()
                .timestamp(System.currentTimeMillis())
                .data(messagesRs).build();
    }

    public CommonRs<MessageRs> getLastMessageRs(Long dialogId, MessageRq messageRq) {
        if (getLastMessage(dialogId).getMessageText().equals(messageRq.getMessageText())) {
            return CommonRs.<MessageRs>builder()
                    .data(createLastMessageRs(dialogsRepository.findById(dialogId).orElseThrow()))
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        return null;
    }

    private Person getRecipientFromDialog(Long authorId, Long dialogId) {
        Dialog dialog = dialogsRepository.findById(dialogId).orElseThrow();
        return !authorId.equals(dialog.getFirstPerson().getId()) ?
                dialog.getFirstPerson() :
                dialog.getSecondPerson();
    }

    private Person findCurrentUser() {
        return personsRepository.findPersonByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow();
    }

    private List<DialogRs> createDialogRsList(Person person) {
        List<DialogRs> dialogRsList = new ArrayList<>();
        List<Dialog> dialogs = getDialogsByPersonId(person.getId());
        for (Dialog d : dialogs) {
            MessageRs messageRs = createLastMessageRs(d);
            DialogRs dialogRs = DialogRs.builder()
                    .id(d.getId())
                    .authorId(messageRs.getAuthorId())
                    .recipientId(messageRs.getRecipientId())
                    .lastMessage(messageRs)
                    .build();
            if (messageRs.getReadStatus() != null) {
                dialogRs.setReadStatus(messageRs.getReadStatus());
            }
            dialogRsList.add(dialogRs);
        }
        return dialogRsList;
    }

    private MessageRs createLastMessageRs(Dialog dialog) {
        try {
            return createMessageRs(getLastMessage(dialog.getId()));
        } catch (Exception e) {
            return MessageRs.builder()
                    .authorId(dialog.getFirstPerson().getId())
                    .recipientId(dialog.getSecondPerson().getId())
                    .recipient(personMapper.toPersonResponse(dialog.getSecondPerson()))
                    .build();
        }
    }

    private Message getLastMessage(Long dialogId) {
        return messagesRepository.findAllByDialogIdAndIsDeletedFalse(dialogId)
                .stream()
                .max(Comparator.comparing(Message::getTime)).orElseThrow();
    }

    private MessageRs createMessageRs(Message message) {
        return MessageRs.builder()
                .id(message.getId())
                .time(message.getTime())
                .isSentByMe(isAuthor(findCurrentUser(), message))
                .authorId(message.getAuthor().getId())
                .recipientId(message.getRecipient().getId())
                .messageText(message.getMessageText())
                .readStatus(message.getReadStatus().name())
                .recipient(personMapper.toPersonResponse(message.getRecipient()))
                .build();
    }

    private List<Dialog> getDialogsByPersonId(Long personId) {
        return dialogsRepository.findAll().stream()
                .filter(d -> d.getFirstPerson().getId().equals(personId) || d.getSecondPerson().getId().equals(personId))
                .collect(Collectors.toList());
    }

    private Boolean isAuthor(Person person, Message message) {
        return message.getAuthor().getId().equals(person.getId());
    }

    private List<DialogRs> blockDialogs(List<DialogRs> dialogs, Person me) {
        List<Friendship> friendships =
                friendshipsRepository.findFriendshipsBySrcPersonIdOrDstPersonIdAndFriendshipStatus(me.getId(), me.getId(), FriendshipStatusTypes.BLOCKED);
        Set<Long> srcDstPersonsIds =  getSrcDstPersonsIds(friendships, me);
        return dialogs
                .stream()
                .filter(dialog -> !srcDstPersonsIds.contains(dialog.getAuthorId()) && !srcDstPersonsIds.contains(dialog.getRecipientId()))
                .collect(Collectors.toList());
    }

    private Set<Long> getSrcDstPersonsIds(List<Friendship> friendships, Person me) {
        Set<Long> ids = new HashSet<>();
        friendships.forEach(friendship -> {
            if (friendship.getDstPerson().getId().equals(me.getId())) {
                ids.add(friendship.getSrcPerson().getId());
            } else {
                ids.add(friendship.getDstPerson().getId());
            }
        });
        return ids;
    }
}
