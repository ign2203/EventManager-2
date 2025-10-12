package org.example.eventmanagermodule.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @NotBlank(message = "login must not be blank")
        @Size(min = 4)
        String login,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8)
        String password
) {
}
