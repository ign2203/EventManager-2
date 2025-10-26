package org.example.eventmanagermodule.User;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(SignUpRequest user) {
        if (userRepository.existsByLogin(user.login())) {
            throw new IllegalArgumentException("Username is already in use");
        }
        var hashedPassword = passwordEncoder.encode(user.password());
        UserEntity UserToSave = new UserEntity(
                null,
                user.login(),
                hashedPassword,
                user.age(),
                UserRole.USER
        );
        userRepository.save(UserToSave);
        log.info("Successfully saved User: {}", UserToSave);
        return toDomain(UserToSave);
    }

    public User findUserById(Long userId) {
        var searchedUserId = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        return toDomain(searchedUserId);
    }

    public User findByLogin(String loginFromToken) {
        UserEntity searchedLogin = userRepository.findByLogin(loginFromToken)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        return toDomain(searchedLogin);
    }

    private static User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getAge(),
                userEntity.getRole()
        );
    }
}