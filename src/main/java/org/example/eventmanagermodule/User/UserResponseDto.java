package org.example.eventmanagermodule.User;

import jakarta.validation.constraints.*;

public record UserResponseDto(
       /*
Исправил учел твои замечания, так как это отдельный класс, то на выход валидность не устанавливаем
и я исправил минимальный порог возраста, так как по ТЗ пользователь не может быть младше 18 лет, также исправил это в сущности энтити
         */
        Long id,
        String login,
        int age,

        UserRole role
) {
}
