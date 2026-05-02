package ru.makolov.otp.service;

import java.util.List;
import java.util.Map;
import ru.makolov.otp.model.dto.OtpConfigRequest;

public interface AdminService {
    Map<String, Object> updateConfig(OtpConfigRequest request);

    List<Map<String, Object>> listUsersWithoutAdmins();

    Map<String, Object> deleteUser(long userId);
}
