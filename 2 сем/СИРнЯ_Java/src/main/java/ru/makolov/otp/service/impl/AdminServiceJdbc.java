package ru.makolov.otp.service.impl;

import java.util.List;
import java.util.Map;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.dao.OtpConfigDao;
import ru.makolov.otp.dao.UserDao;
import ru.makolov.otp.model.OtpConfig;
import ru.makolov.otp.model.UserRecord;
import ru.makolov.otp.model.dto.OtpConfigRequest;
import ru.makolov.otp.service.AdminService;

public class AdminServiceJdbc implements AdminService {
    private final UserDao userDao;
    private final OtpConfigDao otpConfigDao;

    public AdminServiceJdbc(UserDao userDao, OtpConfigDao otpConfigDao) {
        this.userDao = userDao;
        this.otpConfigDao = otpConfigDao;
    }

    @Override
    public Map<String, Object> updateConfig(OtpConfigRequest request) {
        if (request == null || request.codeLength() == null || request.ttlSeconds() == null) {
            throw new ApiException(400, "INVALID_REQUEST", "codeLength and ttlSeconds are required");
        }
        if (request.codeLength() < 4 || request.codeLength() > 10) {
            throw new ApiException(400, "INVALID_CODE_LENGTH", "codeLength must be between 4 and 10");
        }
        if (request.ttlSeconds() < 30 || request.ttlSeconds() > 600) {
            throw new ApiException(400, "INVALID_TTL", "ttlSeconds must be between 30 and 600");
        }

        OtpConfig config = otpConfigDao.upsertConfig(new OtpConfig(request.codeLength(), request.ttlSeconds()));
        return Map.of("codeLength", config.codeLength(), "ttlSeconds", config.ttlSeconds());
    }

    @Override
    public List<Map<String, Object>> listUsersWithoutAdmins() {
        return userDao.findNonAdmins().stream()
                .map(AdminServiceJdbc::toUserMap)
                .toList();
    }

    @Override
    public Map<String, Object> deleteUser(long userId) {
        if (userId <= 0) {
            throw new ApiException(400, "INVALID_USER_ID", "User id must be positive");
        }
        if (userDao.findById(userId).isEmpty()) {
            throw new ApiException(404, "USER_NOT_FOUND", "User not found");
        }
        boolean deleted = userDao.deleteById(userId);
        if (!deleted) {
            throw new ApiException(404, "USER_NOT_FOUND", "User not found");
        }
        return Map.of("deleted", true, "userId", userId);
    }

    private static Map<String, Object> toUserMap(UserRecord userRecord) {
        return Map.of(
                "id", userRecord.id(),
                "login", userRecord.login(),
                "role", userRecord.role().name()
        );
    }
}
