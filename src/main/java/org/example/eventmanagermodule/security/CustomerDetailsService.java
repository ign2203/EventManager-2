package org.example.eventmanagermodule.security;


import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
public class CustomerDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomerDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found"));

        return User
                .withUsername(username)
                .password(userEntity.getPassword())
                .authorities(userEntity.getRole().name())
                .build();
    }
}
