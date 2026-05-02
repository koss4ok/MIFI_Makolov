package ru.makolov.otp.model.dto;

public record RegisterRequest(String login, String password, String role) {
}
