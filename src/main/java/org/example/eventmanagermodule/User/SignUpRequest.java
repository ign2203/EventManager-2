package org.example.eventmanagermodule.User;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "login must not be blank")
        @Size(min = 4, message = "Login cannot be less than 4 characters")
        String login,
        @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "The password cannot be less than 8 characters")
        String password,
        @Min(value = 18, message = "the minimum age cannot be less than 18 years")
        @Max(value = 116, message = "the maximum age cannot be more than 116 years")
        Integer age
) {
}
