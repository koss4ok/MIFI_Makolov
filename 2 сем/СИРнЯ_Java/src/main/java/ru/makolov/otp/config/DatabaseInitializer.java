package ru.makolov.otp.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final AppConfig appConfig;

    public DatabaseInitializer(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void initialize() {
        try (Connection connection = openMainConnectionWithAutoCreate()) {
            runSchema(connection);
            createDefaultAdmin(connection);
            log.info("Database initialization completed");
        } catch (Exception e) {
            throw new IllegalStateException("Database initialization failed", e);
        }
    }

    private Connection openMainConnectionWithAutoCreate() throws SQLException {
        try {
            return DriverManager.getConnection(appConfig.dbUrl(), appConfig.dbUser(), appConfig.dbPassword());
        } catch (SQLException e) {
            if (!"3D000".equals(e.getSQLState())) {
                throw e;
            }

            String databaseName = extractDatabaseName(appConfig.dbUrl());
            String adminUrl = replaceDatabaseName(appConfig.dbUrl(), "postgres");
            log.warn("Database '{}' not found. Trying to create it automatically...", databaseName);

            try (Connection adminConnection = DriverManager.getConnection(adminUrl, appConfig.dbUser(), appConfig.dbPassword());
                    Statement statement = adminConnection.createStatement()) {
                statement.execute("CREATE DATABASE " + quoteIdentifier(databaseName));
                log.info("Database '{}' was created", databaseName);
            } catch (SQLException createError) {
                if (!"42P04".equals(createError.getSQLState())) {
                    throw createError;
                }
                log.info("Database '{}' already exists (race condition)", databaseName);
            }

            return DriverManager.getConnection(appConfig.dbUrl(), appConfig.dbUser(), appConfig.dbPassword());
        }
    }

    private static void runSchema(Connection connection) throws Exception {
        InputStream stream = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql");
        if (stream == null) {
            throw new IllegalStateException("schema.sql not found");
        }
        String sql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            sql = reader.lines().reduce("", (acc, line) -> acc + line + "\n");
        }

        try (Statement statement = connection.createStatement()) {
            for (String part : sql.split(";")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private static void createDefaultAdmin(Connection connection) throws Exception {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery(checkSql)) {
            resultSet.next();
            if (resultSet.getInt(1) > 0) {
                return;
            }
        }

        String passwordHash = BCrypt.hashpw("admin", BCrypt.gensalt());
        String insertSql = "INSERT INTO users(login, password_hash, role) VALUES ('admin', '" + passwordHash + "', 'ADMIN')";
        try (Statement statement = connection.createStatement()) {
            statement.execute(insertSql);
            log.info("Default admin user was created (login=admin, password=admin)");
        }
    }

    private static String extractDatabaseName(String jdbcUrl) {
        int slashIndex = jdbcUrl.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex + 1 >= jdbcUrl.length()) {
            throw new IllegalStateException("Invalid DB url: " + jdbcUrl);
        }
        int queryIndex = jdbcUrl.indexOf('?', slashIndex);
        if (queryIndex < 0) {
            return jdbcUrl.substring(slashIndex + 1);
        }
        return jdbcUrl.substring(slashIndex + 1, queryIndex);
    }

    private static String replaceDatabaseName(String jdbcUrl, String newName) {
        int slashIndex = jdbcUrl.lastIndexOf('/');
        if (slashIndex < 0) {
            throw new IllegalStateException("Invalid DB url: " + jdbcUrl);
        }
        int queryIndex = jdbcUrl.indexOf('?', slashIndex);
        if (queryIndex < 0) {
            return jdbcUrl.substring(0, slashIndex + 1) + newName;
        }
        return jdbcUrl.substring(0, slashIndex + 1) + newName + jdbcUrl.substring(queryIndex);
    }

    private static String quoteIdentifier(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
