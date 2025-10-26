package org.example.eventmanagermodule.User;

public record UserResponseDto(
        Long id,
        String login,
        int age,
        UserRole role
) {
}