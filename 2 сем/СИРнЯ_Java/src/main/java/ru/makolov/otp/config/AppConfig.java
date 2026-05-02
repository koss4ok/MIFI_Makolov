package ru.makolov.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record AppConfig(
        int serverPort,
        String fileChannelPath,
        String outboxDir,
        String dbUrl,
        String dbUser,
        String dbPassword,
        String jwtSecret,
        long jwtTtlSeconds) {
    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_FILE_CHANNEL_PATH = "otp-codes.log";
    private static final String DEFAULT_OUTBOX_DIR = "notifications-outbox";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/makolov_otp";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final String DEFAULT_JWT_SECRET = "change_me_to_long_secure_secret_for_prod_123456";
    private static final String DEFAULT_JWT_TTL_SECONDS = "3600";

    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream stream = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
        }

        int serverPort = Integer.parseInt(readValue(properties, "server.port", "SERVER_PORT", DEFAULT_PORT));
        String fileChannelPath = readValue(properties, "file.channel.path", "FILE_CHANNEL_PATH", DEFAULT_FILE_CHANNEL_PATH);
        String outboxDir = readValue(properties, "outbox.dir", "OUTBOX_DIR", DEFAULT_OUTBOX_DIR);
        String dbUrl = readValue(properties, "db.url", "DB_URL", DEFAULT_DB_URL);
        String dbUser = readValue(properties, "db.user", "DB_USER", DEFAULT_DB_USER);
        String dbPassword = readValue(properties, "db.password", "DB_PASSWORD", DEFAULT_DB_PASSWORD);
        String jwtSecret = readValue(properties, "jwt.secret", "JWT_SECRET", DEFAULT_JWT_SECRET);
        long jwtTtlSeconds = Long.parseLong(readValue(properties, "jwt.ttl.seconds", "JWT_TTL_SECONDS", DEFAULT_JWT_TTL_SECONDS));
        return new AppConfig(serverPort, fileChannelPath, outboxDir, dbUrl, dbUser, dbPassword, jwtSecret, jwtTtlSeconds);
    }

    private static String readValue(Properties properties, String propertyName, String envName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return properties.getProperty(propertyName, defaultValue);
    }
}
