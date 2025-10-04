package org.example.eventmanagermodule.User;

public record User (
        Long Id,
        String login,
        int age,
        UserRole role
) {
}
