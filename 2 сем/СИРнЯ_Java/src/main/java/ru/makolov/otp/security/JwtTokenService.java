package ru.makolov.otp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import ru.makolov.otp.model.Role;
import ru.makolov.otp.model.UserRecord;

public class JwtTokenService {
    private final SecretKey key;
    private final long ttlSeconds;

    public JwtTokenService(String secret, long ttlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issueToken(UserRecord userRecord) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .subject(userRecord.login())
                .claim("uid", userRecord.id())
                .claim("role", userRecord.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    public static Role readRole(Claims claims) {
        return Role.valueOf(claims.get("role", String.class));
    }

    public static long readUserId(Claims claims) {
        Object value = claims.get("uid");
        if (value instanceof Integer i) {
            return i.longValue();
        }
        if (value instanceof Long l) {
            return l;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
