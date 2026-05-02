package ru.makolov.otp.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.makolov.otp.dao.UserDao;
import ru.makolov.otp.model.Role;
import ru.makolov.otp.model.UserRecord;

public class JdbcUserDao extends JdbcSupport implements UserDao {
    public JdbcUserDao(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public Optional<UserRecord> findByLogin(String login) {
        String sql = "SELECT id, login, password_hash, role FROM users WHERE login = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot find user by login", e);
        }
    }

    @Override
    public Optional<UserRecord> findById(long id) {
        String sql = "SELECT id, login, password_hash, role FROM users WHERE id = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUser(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot find user by id", e);
        }
    }

    @Override
    public UserRecord save(UserRecord userRecord) {
        String sql = "INSERT INTO users(login, password_hash, role) VALUES (?, ?, ?) RETURNING id, login, password_hash, role";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userRecord.login());
            statement.setString(2, userRecord.passwordHash());
            statement.setString(3, userRecord.role().name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Cannot read saved user");
                }
                return mapUser(resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save user", e);
        }
    }

    @Override
    public boolean existsAdmin() {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN')";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot check admin existence", e);
        }
    }

    @Override
    public List<UserRecord> findNonAdmins() {
        String sql = "SELECT id, login, password_hash, role FROM users WHERE role <> 'ADMIN' ORDER BY id";
        List<UserRecord> users = new ArrayList<>();
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot load users", e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (var connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot delete user", e);
        }
    }

    private static UserRecord mapUser(ResultSet resultSet) throws SQLException {
        return new UserRecord(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role"))
        );
    }
}
