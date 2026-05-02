package ru.makolov.otp.model.dto;

public record OtpValidateRequest(String operationId, String code) {
}
