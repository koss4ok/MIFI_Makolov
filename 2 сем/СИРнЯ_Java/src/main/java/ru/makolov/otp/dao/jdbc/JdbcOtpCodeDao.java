package ru.makolov.otp.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.makolov.otp.dao.OtpCodeDao;
import ru.makolov.otp.model.OtpRecord;
import ru.makolov.otp.model.OtpStatus;

public class JdbcOtpCodeDao extends JdbcSupport implements OtpCodeDao {
    public JdbcOtpCodeDao(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public OtpRecord save(OtpRecord otpRecord) {
        String sql = "INSERT INTO otp_codes(user_id, operation_id, code_hash, channel, destination, status, expires_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (operation_id) DO UPDATE SET "
                + "user_id = EXCLUDED.user_id, code_hash = EXCLUDED.code_hash, channel = EXCLUDED.channel, destination = EXCLUDED.destination, "
                + "status = EXCLUDED.status, expires_at = EXCLUDED.expires_at, created_at = NOW(), used_at = NULL "
                + "RETURNING id, user_id, operation_id, code_hash, channel, destination, status, expires_at, created_at, used_at";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, otpRecord.userId());
            statement.setString(2, otpRecord.operationId());
            statement.setString(3, otpRecord.code());
            statement.setString(4, otpRecord.channel());
            statement.setString(5, otpRecord.destination());
            statement.setString(6, otpRecord.status().name());
            statement.setObject(7, otpRecord.expiresAt());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return mapOtp(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save otp code", e);
        }
    }

    @Override
    public Optional<OtpRecord> findByOperationId(String operationId) {
        String sql = "SELECT id, user_id, operation_id, code_hash, channel, destination, status, expires_at, created_at, used_at "
                + "FROM otp_codes WHERE operation_id = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapOtp(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load otp code", e);
        }
    }

    @Override
    public void updateStatus(String operationId, String status) {
        String sql = "UPDATE otp_codes SET status = ?, used_at = CASE WHEN ? = 'USED' THEN NOW() ELSE used_at END WHERE operation_id = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setString(3, operationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot update otp status", e);
        }
    }

    @Override
    public int expireActiveCodes() {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < NOW()";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot expire otp codes", e);
        }
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot delete user otp codes", e);
        }
    }

    @Override
    public List<OtpRecord> findByUserId(long userId) {
        String sql = "SELECT id, user_id, operation_id, code_hash, channel, destination, status, expires_at, created_at, used_at "
                + "FROM otp_codes WHERE user_id = ? ORDER BY created_at DESC";
        List<OtpRecord> list = new ArrayList<>();
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapOtp(resultSet));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load user otp codes", e);
        }
    }

    private static OtpRecord mapOtp(ResultSet resultSet) throws SQLException {
        Instant usedAt = resultSet.getTimestamp("used_at") == null
                ? null
                : resultSet.getTimestamp("used_at").toInstant();

        return new OtpRecord(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code_hash"),
                resultSet.getString("channel"),
                resultSet.getString("destination"),
                OtpStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("expires_at").toInstant(),
                resultSet.getTimestamp("created_at").toInstant(),
                usedAt
        );
    }
}
