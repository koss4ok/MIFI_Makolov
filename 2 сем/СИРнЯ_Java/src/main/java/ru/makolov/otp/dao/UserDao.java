package ru.makolov.otp.dao;

import java.util.List;
import java.util.Optional;
import ru.makolov.otp.model.UserRecord;

public interface UserDao {
    Optional<UserRecord> findByLogin(String login);

    Optional<UserRecord> findById(long id);

    UserRecord save(UserRecord userRecord);

    boolean existsAdmin();

    List<UserRecord> findNonAdmins();

    boolean deleteById(long id);
}
