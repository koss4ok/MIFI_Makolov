package ru.makolov.otp.model;

public record AuthContext(long userId, String login, Role role) {
}
