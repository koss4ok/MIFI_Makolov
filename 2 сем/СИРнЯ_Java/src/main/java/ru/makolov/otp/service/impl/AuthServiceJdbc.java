package ru.makolov.otp.service.impl;

import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.dao.UserDao;
import ru.makolov.otp.model.Role;
import ru.makolov.otp.model.UserRecord;
import ru.makolov.otp.model.dto.LoginRequest;
import ru.makolov.otp.model.dto.RegisterRequest;
import ru.makolov.otp.security.JwtTokenService;
import ru.makolov.otp.service.AuthService;

public class AuthServiceJdbc implements AuthService {
    private final UserDao userDao;
    private final JwtTokenService jwtTokenService;

    public AuthServiceJdbc(UserDao userDao, JwtTokenService jwtTokenService) {
        this.userDao = userDao;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userDao.findByLogin(request.login()).isPresent()) {
            throw new ApiException(409, "USER_EXISTS", "User with this login already exists");
        }

        Role role = parseRole(request.role());
        if (role == Role.ADMIN && userDao.existsAdmin()) {
            throw new ApiException(409, "ADMIN_EXISTS", "Second admin cannot be registered");
        }

        String passwordHash = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        UserRecord saved = userDao.save(new UserRecord(0, request.login(), passwordHash, role));

        return Map.of(
                "id", saved.id(),
                "login", saved.login(),
                "role", saved.role().name()
        );
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        if (request == null || isBlank(request.login()) || isBlank(request.password())) {
            throw new ApiException(400, "INVALID_REQUEST", "login and password are required");
        }

        UserRecord user = userDao.findByLogin(request.login())
                .orElseThrow(() -> new ApiException(401, "BAD_CREDENTIALS", "Invalid credentials"));

        if (!BCrypt.checkpw(request.password(), user.passwordHash())) {
            throw new ApiException(401, "BAD_CREDENTIALS", "Invalid credentials");
        }

        String token = jwtTokenService.issueToken(user);
        return Map.of(
                "token", token,
                "expiresInSeconds", jwtTokenService.ttlSeconds(),
                "tokenType", "Bearer"
        );
    }

    private static void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.login()) || isBlank(request.password()) || isBlank(request.role())) {
            throw new ApiException(400, "INVALID_REQUEST", "login, password and role are required");
        }
    }

    private static Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            throw new ApiException(400, "INVALID_ROLE", "Role must be ADMIN or USER");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
