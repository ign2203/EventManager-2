package org.example.eventmanagermodule.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;


//JwtTokenFilter — это фильтр HTTP-запросов, который проверяет JWT на каждом запросе к серверу.
@Component
public class JwtTokenFilter extends OncePerRequestFilter {
/*
OncePerRequestFilter
Это класс из Spring Security.
Гарантирует, что фильтр сработает только один раз на один HTTP-запрос.


 */

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtTokenManager jwtTokenManager;
    private final UserService userService;

    public JwtTokenFilter(
            JwtTokenManager jwtTokenManager,
            @Lazy UserService userService) { //"создай этот бин только тогда, когда он реально нужен".
        this.jwtTokenManager = jwtTokenManager;
        this.userService = userService;
    }

    //Это метод, который Spring вызывает для каждого запроса.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // запрос
            HttpServletResponse response, // ответ на запрос
            FilterChain filterChain // это цепочка всех фильтров, через которую должен пройти запрос.
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);//вытаскиваем из Header JWT  в формате String
        //Проверяет формат: токен не пустой и начинается с Bearer .
        // Логика: если нет заголовка или он не начинается с Bearer → просто пропускаем запрос дальше без проверки токена.
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); //
            return;
        }
        var jwtToken = authorizationHeader.substring(7);// далее получаем jwt Токен без Bearer и сохраняем в jwtToken
        String loginFromToken; // болванка

        //Парсит токен через JwtTokenManager → получает логин пользователя
        try {
            loginFromToken = jwtTokenManager.getLoginFromToken(jwtToken); // далее выдергиваем логин из токена по методу jwtTokenManager
        } catch (Exception e) { // блок в случае если токен невалидный
            logger.error("Invalid JWT Token", e);
            filterChain.doFilter(request, response);
            return;
        }
       // Проверяет, есть ли такой пользователь в БД (через UserService).
        User user = userService.findByLogin(loginFromToken);

//Создаёт UsernamePasswordAuthenticationToken с пользователем и ролями.
//Это класс из Spring Security, который представляет аутентифицированного пользователя.
        //По сути, это контейнер, который говорит Spring Security:
        // “Вот кто пользователь, вот его роли, он аутентифицирован”.
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user, // Вот кто пользователь
                null, // credentials — пароль не нужен, т.к. проверка токена уже прошла
                List.of(new SimpleGrantedAuthority((user.role().toString()))));// вот его роли
        //Кладёт объект аутентификации в SecurityContext → теперь Spring Security знает, кто пользователь.
        SecurityContextHolder.getContext()
                .setAuthentication(token);

//Пропускает запрос дальше по цепочке фильтров (filterChain.doFilter) → дальше включаются правила из SecurityFilterChain и контроллеры
        filterChain.doFilter(request, response);


    }
}
