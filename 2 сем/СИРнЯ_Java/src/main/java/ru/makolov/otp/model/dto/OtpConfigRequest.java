package ru.makolov.otp.model.dto;

public record OtpConfigRequest(Integer codeLength, Integer ttlSeconds) {
}
