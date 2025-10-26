package org.example.eventmanagermodule.User;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void creatingDefaultUsers() {

        if (userRepository.findByLogin("admin").isEmpty()) {
            var userRoleAdmin = new UserEntity(
                    null,
                    "admin",
                    passwordEncoder.encode("admin1234"),
                    99,
                    UserRole.ADMIN
            );
            userRepository.save(userRoleAdmin);
        }
        if (userRepository.findByLogin("user").isEmpty()) {
            var userRoleUser = new UserEntity(
                    null,
                    "user",
                    passwordEncoder.encode("user1234"),
                    99,
                    UserRole.USER
            );
            userRepository.save(userRoleUser);
        }
    }
}