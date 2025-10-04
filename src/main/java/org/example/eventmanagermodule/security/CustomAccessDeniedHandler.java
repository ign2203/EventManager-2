package org.example.eventmanagermodule.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eventmanagermodule.Location.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
//🚫 Этот обработчик вызывается, если пользователь аутентифицирован, но не имеет прав.
//Пользователь вошёл, но доступ запрещён
//403
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        log.error("Handling Access denied authenticate " , accessDeniedException);

        var messageError = new ErrorMessageResponse(
                "FORBIDDEN",
                accessDeniedException.getMessage(),
                LocalDateTime.now()
        );
        var stringResponse = objectMapper.writeValueAsString(messageError);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);//«Ответ придёт в формате JSON».
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);// Устанавливаем HTTP-код ответа 403 Forbidden.Это стандарт: сервер понял запрос, но отказывает в доступе (у пользователя нет нужных прав).
        response.getWriter().println(stringResponse);// строку в формате JSON — и реально «пишем» её в тело HTTP-ответа.
    }
    /*
    Разбор, метод срабатывает на момент запроса, до его вызова, чтобы он сработал
    он должен понимать запрос, выдавать ответ, и понимать какое исключение
    Если в принципе мы его ловим, значит у пользователя нет прав для выполннения запроса
    далее обрабатываем ошибку, читаемом формате. FORBIDDEN - запрещено
    Далее, чтобы выдать ее в формате json, мы ее должны перевести из DTO в json, помогает нам ObjectMapper(Jackson)
     */
}
