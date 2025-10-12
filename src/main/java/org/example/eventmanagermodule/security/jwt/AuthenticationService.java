package org.example.eventmanagermodule.security.jwt;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserLoginRequest;
import org.example.eventmanagermodule.User.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final JwtTokenManager jwtTokenManager;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthenticationService(JwtTokenManager jwtTokenManager, AuthenticationManager authenticationManager, UserService userService) {
        this.jwtTokenManager = jwtTokenManager;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    public String authenticateUser(
            @Valid UserLoginRequest userLoginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userLoginRequest.login(),
                userLoginRequest.password()
        ));
        User user = userService.findByLogin(userLoginRequest.login());
        return jwtTokenManager.generateToken(user.login(), user.role());
    }
}
