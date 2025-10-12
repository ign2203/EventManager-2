package org.example.eventmanagermodule.security;

import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomerDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomerDetailsService.class);
    private final UserRepository userRepository;

    public CustomerDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            log.warn("Username is null or blank, request will be rejected");
            throw new UsernameNotFoundException("Username is null or empty");
        }
        UserEntity userEntity = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException
                        ("User not found with username: " + username));
        log.info("Successfully loaded CustomerDetails for username: {}", username);
        return User
                .withUsername(userEntity.getLogin())
                .password(userEntity.getPassword())
                .authorities(userEntity.getRole().name())
                .build();
    }
}
