package ru.makolov.otp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Locale;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.model.AuthContext;
import ru.makolov.otp.model.Role;

public class JwtAuthService {
    private final JwtTokenService jwtTokenService;

    public JwtAuthService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public AuthContext requireUser(String authorizationHeader) {
        String token = readBearerToken(authorizationHeader);
        try {
            Claims claims = jwtTokenService.parse(token);
            return new AuthContext(
                    JwtTokenService.readUserId(claims),
                    claims.getSubject(),
                    JwtTokenService.readRole(claims)
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(401, "UNAUTHORIZED", "Invalid or expired bearer token");
        }
    }

    public AuthContext requireAdmin(String authorizationHeader) {
        AuthContext authContext = requireUser(authorizationHeader);
        if (authContext.role() != Role.ADMIN) {
            throw new ApiException(403, "FORBIDDEN", "Admin role is required");
        }
        return authContext;
    }

    private static String readBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ApiException(401, "UNAUTHORIZED", "Missing bearer token");
        }
        String normalized = authorizationHeader.toLowerCase(Locale.ROOT);
        String prefix = "bearer ";
        if (!normalized.startsWith(prefix)) {
            throw new ApiException(401, "UNAUTHORIZED", "Missing bearer token");
        }
        return authorizationHeader.substring(prefix.length()).trim();
    }
}
