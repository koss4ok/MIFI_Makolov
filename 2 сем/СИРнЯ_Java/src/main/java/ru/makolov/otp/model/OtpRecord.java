package ru.makolov.otp.model;

import java.time.Instant;

public record OtpRecord(
        long id,
        long userId,
        String operationId,
        String code,
        String channel,
        String destination,
        OtpStatus status,
        Instant expiresAt,
        Instant createdAt,
        Instant usedAt) {
}
