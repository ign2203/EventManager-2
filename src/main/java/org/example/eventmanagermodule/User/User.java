package org.example.eventmanagermodule.User;

public record User (
        Long id,
        String login,
        int age,
        UserRole role
) {
}
