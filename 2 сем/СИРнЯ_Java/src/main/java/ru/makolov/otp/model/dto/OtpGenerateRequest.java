package ru.makolov.otp.model.dto;

public record OtpGenerateRequest(String operationId, String channel, String destination) {
}
