package org.example.eventmanagermodule.Events.Registration;

import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventRegistrationService {
    private final EventRegistrationRepository registrationRepository;

    public EventRegistrationService(EventRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

     public List<UserEntity> getAllUsersRegisterEvent(Long eventId) {
         return registrationRepository.findUsersByEventId(eventId);
    }

}
