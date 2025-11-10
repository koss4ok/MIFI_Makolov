package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class UserStoreTest {

    @TempDir Path tmp;

    @Test
    void hashVerifyWorks() {
        String h = UserStore.hash("test123");
        assertTrue(UserStore.verify("test123",h));
        assertFalse(UserStore.verify("bad",h));
    }

    @Test
    void persistUsersAndReload() {
        Path file = tmp.resolve("users.json");

        UserStore store1 = new UserStore(file.toString());
        store1.put("alice",UserStore.hash("p1"));
        store1.put("bob",UserStore.hash("p2"));

        UserStore store2 = new UserStore(file.toString());
        assertTrue(store2.exists("alice"));
        assertTrue(store2.exists("bob"));
        assertTrue(UserStore.verify("p2",store2.get("bob")));
    }
}
