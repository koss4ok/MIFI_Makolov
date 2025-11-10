package org.example;

public class AuthService {
    private final UserStore store;

    public AuthService(UserStore store) {
        this.store = store;
    }

    public boolean userExists(String login) {
        return store.exists(login);
    }

    public void register(String login, String password) throws Exception {
        if (login == null || login.isBlank())
            throw new Exception("Логин пуст.");
        if (password == null || password.length() < 4)
            throw new Exception("Пароль слишком короткий (мин. 4).");
        if (store.exists(login))
            throw new Exception("Пользователь уже существует.");
        String hh = UserStore.hash(password);
        store.put(login, hh);
    }

    public boolean login(String login, String password) {
        if (!store.exists(login))
            return false;
        String hh = store.get(login);
        return UserStore.verify(password, hh);
    }
}
