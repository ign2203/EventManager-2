package org.example.eventmanagermodule.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.eventmanagermodule.Location.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;


//Обработчик ошибок аутентификации — если пользователь не вошёл в систему, но пытается попасть в защищённый эндпоинт
//(то есть не вошёл в систему, токен не передал или он просрочен).
//401
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        log.error("Handling Authentication exception");

        var message = new ErrorMessageResponse(
                "Failed to authenticate",
                authException.getMessage(),
                LocalDateTime.now()
        );
        var jsonMessage = objectMapper.writeValueAsString(message);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);// → говорим клиенту: ответ будет в формате JSON.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// код 401 Unauthorized, стандартный ответ, когда пользователь не вошёл в систему.
        response.getWriter().write(jsonMessage);//отправляем сам JSON-текст клиенту (тело ответа).
    }
}
