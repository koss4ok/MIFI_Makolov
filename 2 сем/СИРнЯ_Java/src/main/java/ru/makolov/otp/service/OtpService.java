package ru.makolov.otp.service;

import java.util.Map;
import ru.makolov.otp.model.AuthContext;
import ru.makolov.otp.model.dto.OtpGenerateRequest;
import ru.makolov.otp.model.dto.OtpValidateRequest;

public interface OtpService {
    Map<String, Object> generate(AuthContext authContext, OtpGenerateRequest request);

    Map<String, Object> validate(AuthContext authContext, OtpValidateRequest request);
}
