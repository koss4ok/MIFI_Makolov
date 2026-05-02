package ru.makolov.otp.service;

import java.util.Map;
import ru.makolov.otp.model.dto.LoginRequest;
import ru.makolov.otp.model.dto.RegisterRequest;

public interface AuthService {
    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);
}
