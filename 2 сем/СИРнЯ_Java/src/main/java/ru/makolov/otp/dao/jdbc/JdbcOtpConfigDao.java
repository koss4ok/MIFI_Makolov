package ru.makolov.otp.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import ru.makolov.otp.dao.OtpConfigDao;
import ru.makolov.otp.model.OtpConfig;

public class JdbcOtpConfigDao extends JdbcSupport implements OtpConfigDao {
    public JdbcOtpConfigDao(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public OtpConfig getConfig() {
        String sql = "SELECT code_length, ttl_seconds FROM otp_config WHERE id = 1";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new OtpConfig(resultSet.getInt("code_length"), resultSet.getInt("ttl_seconds"));
            }
            return new OtpConfig(6, 120);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot read otp config", e);
        }
    }

    @Override
    public OtpConfig upsertConfig(OtpConfig otpConfig) {
        String sql = "INSERT INTO otp_config(id, code_length, ttl_seconds, updated_at) VALUES (1, ?, ?, NOW()) "
                + "ON CONFLICT (id) DO UPDATE SET code_length = EXCLUDED.code_length, ttl_seconds = EXCLUDED.ttl_seconds, updated_at = NOW()";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, otpConfig.codeLength());
            statement.setInt(2, otpConfig.ttlSeconds());
            statement.executeUpdate();
            return otpConfig;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save otp config", e);
        }
    }
}
