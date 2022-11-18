package main.service;

import liquibase.util.file.FilenameUtils;
import lombok.AllArgsConstructor;

import main.api.response.PersonResponse;
import main.api.response.UserRs;
import main.model.entities.Person;
import main.repository.CaptchaRepository;
import main.repository.PersonsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;


@Service
@AllArgsConstructor
public class UsersService {
    private final PersonsRepository personsRepository;
    private final CaptchaRepository captchaRepository;
    private final CloudaryService cloudaryService;

    public UserRs editImage(Principal principal, MultipartFile photo, String phone, String about,
                            String city, String country, String first_name, String last_name,
                            String birth_date, String message_permission) throws IOException {
        Person person = personsRepository.findPersonByEmail(principal.getName()).get();
        UserRs response =new UserRs();
        PersonResponse personResponse = new PersonResponse(person);

        String extension = (photo.getOriginalFilename());
        cloudaryService.uploadImage((File) photo);
        personResponse.setPhoto(cloudaryService.getImage(photo.getOriginalFilename()));

        return response;
    }


}
