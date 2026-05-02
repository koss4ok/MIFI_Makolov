package ru.makolov.otp.model;

public record OtpConfig(int codeLength, int ttlSeconds) {
}
