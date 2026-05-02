package ru.makolov.otp.service.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.dao.OtpCodeDao;
import ru.makolov.otp.dao.OtpConfigDao;
import ru.makolov.otp.model.AuthContext;
import ru.makolov.otp.model.OtpConfig;
import ru.makolov.otp.model.OtpRecord;
import ru.makolov.otp.model.OtpStatus;
import ru.makolov.otp.model.dto.OtpGenerateRequest;
import ru.makolov.otp.model.dto.OtpValidateRequest;
import ru.makolov.otp.service.OtpService;
import ru.makolov.otp.service.channel.NotificationChannel;
import ru.makolov.otp.util.RandomCodeGenerator;

public class OtpServiceJdbc implements OtpService {
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final Map<String, NotificationChannel> channels = new HashMap<>();

    public OtpServiceJdbc(
            OtpCodeDao otpCodeDao,
            OtpConfigDao otpConfigDao,
            NotificationChannel email,
            NotificationChannel sms,
            NotificationChannel telegram,
            NotificationChannel file) {
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        List.of(email, sms, telegram, file).forEach(channel -> channels.put(channel.channel(), channel));
    }

    @Override
    public Map<String, Object> generate(AuthContext authContext, OtpGenerateRequest request) {
        if (request == null || isBlank(request.operationId()) || isBlank(request.channel()) || isBlank(request.destination())) {
            throw new ApiException(400, "INVALID_REQUEST", "operationId, channel and destination are required");
        }

        String requestedChannel = request.channel().toLowerCase();
        NotificationChannel channel = channels.get(requestedChannel);
        if (channel == null) {
            throw new ApiException(400, "INVALID_CHANNEL", "Supported channels: email, sms, telegram, file");
        }

        OtpConfig otpConfig = otpConfigDao.getConfig();
        String code = RandomCodeGenerator.digits(otpConfig.codeLength());
        String codeHash = BCrypt.hashpw(code, BCrypt.gensalt());

        OtpRecord saved = otpCodeDao.save(new OtpRecord(
                0,
                authContext.userId(),
                request.operationId(),
                codeHash,
                requestedChannel,
                request.destination(),
                OtpStatus.ACTIVE,
                Instant.now().plusSeconds(otpConfig.ttlSeconds()),
                Instant.now(),
                null
        ));

        channel.sendCode(request.destination(), code);

        return Map.of(
                "otpId", saved.id(),
                "operationId", saved.operationId(),
                "status", saved.status().name(),
                "expiresAt", saved.expiresAt().toString(),
                "channel", saved.channel()
        );
    }

    @Override
    public Map<String, Object> validate(AuthContext authContext, OtpValidateRequest request) {
        if (request == null || isBlank(request.operationId()) || isBlank(request.code())) {
            throw new ApiException(400, "INVALID_REQUEST", "operationId and code are required");
        }

        OtpRecord otpRecord = otpCodeDao.findByOperationId(request.operationId())
                .orElseThrow(() -> new ApiException(404, "OTP_NOT_FOUND", "OTP for operation not found"));

        if (otpRecord.userId() != authContext.userId()) {
            throw new ApiException(403, "FORBIDDEN", "OTP belongs to another user");
        }
        if (otpRecord.status() == OtpStatus.USED) {
            throw new ApiException(409, "OTP_USED", "OTP is already used");
        }
        if (otpRecord.status() == OtpStatus.EXPIRED || otpRecord.expiresAt().isBefore(Instant.now())) {
            otpCodeDao.updateStatus(request.operationId(), OtpStatus.EXPIRED.name());
            throw new ApiException(409, "OTP_EXPIRED", "OTP is expired");
        }
        if (!BCrypt.checkpw(request.code(), otpRecord.code())) {
            throw new ApiException(400, "OTP_INVALID", "OTP code is invalid");
        }

        otpCodeDao.updateStatus(request.operationId(), OtpStatus.USED.name());
        return Map.of(
                "operationId", request.operationId(),
                "status", OtpStatus.USED.name(),
                "validated", true
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
