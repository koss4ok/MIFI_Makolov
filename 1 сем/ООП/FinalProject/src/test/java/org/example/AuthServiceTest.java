package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @TempDir Path tmp;

    @Test
    void registerLoginWorks() throws Exception {
        UserStore store = new UserStore(tmp.resolve("users.json").toString());
        AuthService auth = new AuthService(store);

        auth.register("u1","qwerty");

        assertTrue(auth.login("u1","qwerty"));
        assertFalse(auth.login("u1","wrong"));
    }

    @Test
    void duplicateRegisterThrows() throws Exception {
        UserStore store = new UserStore(tmp.resolve("users.json").toString());
        AuthService auth = new AuthService(store);

        auth.register("u1","pass");
        assertThrows(Exception.class, () -> auth.register("u1","pass"));
    }

    @Test
    void shortPasswordRejected() {
        UserStore store = new UserStore(tmp.resolve("users.json").toString());
        AuthService auth = new AuthService(store);

        assertThrows(Exception.class, () -> auth.register("u2","123"));
    }
}
