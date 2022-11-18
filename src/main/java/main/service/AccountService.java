package main.service;

import lombok.AllArgsConstructor;
import main.api.request.RegisterRq;
import main.api.response.ComplexRs;
import main.api.response.RegisterRs;
import main.model.entities.Captcha;
import main.model.entities.Person;
import main.model.enums.MessagePermissionTypes;
import main.repository.CaptchaRepository;
import main.repository.PersonsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AccountService {
    private final PersonsRepository personsRepository;
    private final CaptchaRepository captchaRepository;

    public RegisterRs getRegResponse(RegisterRq regRequest){
        RegisterRs registerRs = new RegisterRs();
        ComplexRs data = new ComplexRs();
        data.setMessage("OK");

        if (!regRequest.getPasswd1().equals(regRequest.getPasswd2())){
            registerRs.setError("Ошибка в пароле!");
            registerRs.setError_description("Введены разные пароли");
            data.setMessage("404");
        }
        String captcha = regRequest.getCode();
        String secret = regRequest.getCodeSecret();
        Optional<Captcha> optionalCaptcha = captchaRepository.findCaptchaBySecretCode(secret);
        if (optionalCaptcha.isPresent()) {
            if (!optionalCaptcha.get().getCode().equals(captcha)) {
                registerRs.setError("Каптча");
                registerRs.setError_description("Код с картинки введён неверно");
            }
        } else {
            registerRs.setError("Каптча");
            registerRs.setError_description("код устарел");
        }

        registerRs.setEmail(regRequest.getEmail());
        registerRs.setData(data);

  //      if (registerRs.getError().equals(null)) {
            Person person = new Person();
            person.setFirstName(regRequest.getFirstName());
            person.setLastName(regRequest.getLastName());
            person.setPassword(regRequest.getPasswd1());
            person.setRegDate(LocalDateTime.now());
            person.setEmail(regRequest.getEmail());
            person.setPhoto("/uploadDefault/default.png");
            person.setIsApproved(false);
            person.setIsBlocked(false);
            person.setIsDeleted(false);
            person.setEmail(regRequest.getEmail());
            person.setMessagePermission(MessagePermissionTypes.ALL);
            personsRepository.save(person);
   //     }

        return registerRs;
    }
}
