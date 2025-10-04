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
        private final AuthenticationManager authenticationManager; // что за функциональный интерфейс для чего он нужен не понятно?

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
        /*
        разбор метода, он нужен аутентификации в самом контроллере
        на входе получает логин и пароль, на выходе сам токен
        используется при этом UsernamePasswordAuthenticationToken, чтобы спринг сохранил его в контекст
          */

/*
DTO нужен только для выходного ответа клиенту, а метод getCurrentAuthenticatedUserOrThrow()
 — это внутренний вспомогательный метод, чтобы работать с пользователем в сервисе или контроллере.
 */
        public User getCurrentAuthenticatedUserOrThrow() {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                throw new IllegalStateException("Authentication Failed");
            }
            return (User) authentication.
                    getPrincipal(); // не понимаю, что возвращает, что такое getPrincipal?
        }
    }
