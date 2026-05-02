package ru.makolov.otp.dao;

import ru.makolov.otp.model.OtpConfig;

public interface OtpConfigDao {
    OtpConfig getConfig();

    OtpConfig upsertConfig(OtpConfig otpConfig);
}
