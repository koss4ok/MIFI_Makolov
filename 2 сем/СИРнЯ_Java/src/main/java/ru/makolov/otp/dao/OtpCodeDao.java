package ru.makolov.otp.dao;

import java.util.List;
import java.util.Optional;
import ru.makolov.otp.model.OtpRecord;

public interface OtpCodeDao {
    OtpRecord save(OtpRecord otpRecord);

    Optional<OtpRecord> findByOperationId(String operationId);

    void updateStatus(String operationId, String status);

    int expireActiveCodes();

    void deleteByUserId(long userId);

    List<OtpRecord> findByUserId(long userId);
}
