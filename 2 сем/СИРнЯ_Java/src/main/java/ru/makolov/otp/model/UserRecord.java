package ru.makolov.otp.model;

public record UserRecord(long id, String login, String passwordHash, Role role) {
}
