package org.example.eventmanagermodule.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    private final UserService userService;
    private final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    public SecurityUtils(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("The user is not authenticated");
        }
        String username = authentication.getName();
        log.debug("Authenticated user: {}", username);
        return userService.findByLogin(username);
    }
}