package org.example.eventmanagermodule.security.jwt;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserLoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final JwtTokenManager jwtTokenManager;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JwtTokenManager jwtTokenManager, AuthenticationManager authenticationManager) {
        this.jwtTokenManager = jwtTokenManager;
        this.authenticationManager = authenticationManager;
    }


    public String authenticateUser(
            @Valid UserLoginRequest userLoginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLoginRequest.login(),
                userLoginRequest.password()
        ));
        return jwtTokenManager.generateToken(userLoginRequest.login());
    }


    public User getCurrentAuthenticatedUserOrThrow() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("Authentication Failed");
        }
        return (User) authentication.
                getPrincipal();
    }
}
