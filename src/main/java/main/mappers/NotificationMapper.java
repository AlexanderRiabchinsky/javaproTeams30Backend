package main.mappers;

import main.api.response.NotificationResponse;
import main.model.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PersonMapper.class})
public interface NotificationMapper {

    @Mapping(target = "info", constant = "notificationInfo")
    @Mapping(target = "notificationType", source = "entity.notificationType")
    @Mapping(target = "entityAuthor", source = "person")
    NotificationResponse toNotificationResponse(Notification notification);
}