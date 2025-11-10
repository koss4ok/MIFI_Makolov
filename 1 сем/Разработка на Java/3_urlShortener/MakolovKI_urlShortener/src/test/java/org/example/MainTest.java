package org.example;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.Scanner;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private Path originalCsvPath;
    private Path testCsvPath;
    private Scanner originalScanner;
    private String originalDomain;
    private UUID originalUserId;

    private static Method getPrivateMethod(String name, Class<?>... params) throws NoSuchMethodException {
        Method m = Main.class.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    @BeforeEach
    void setUp() throws Exception {
        // CSV на тестовый
        originalCsvPath = Main.csvPath;
        testCsvPath = Path.of("./DB_test.csv");
        Main.csvPath = testCsvPath;
        Files.deleteIfExists(testCsvPath);
        Files.writeString(testCsvPath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Scanner / domain / userId
        originalScanner = Main.scanner;
        originalDomain = Main.domain;
        originalUserId = Main.currentUserId;
    }

    @AfterEach
    void tearDown() throws Exception {
        // восстановление
        Main.scanner = originalScanner;
        Main.domain = originalDomain;
        Main.currentUserId = originalUserId;
        Main.csvPath = originalCsvPath;

        Files.deleteIfExists(testCsvPath);
    }

    // 1) isShort возвращает true для своих коротких ссылок
    @Test
    void isShort_true_forOwnDomain() throws Exception {
        String url = Main.domain + "abcdef0123";
        Method isShort = getPrivateMethod("isShort", String.class);
        boolean res = (boolean) isShort.invoke(null, url);
        assertTrue(res);
    }

    // 2) isShort возвращает false для внешних ссылок
    @Test
    void isShort_false_forExternal() throws Exception {
        Method isShort = getPrivateMethod("isShort", String.class);
        boolean res = (boolean) isShort.invoke(null, "https://google.com/");
        assertFalse(res);
    }

    // 3) auth с пустым вводом генерирует UUID
    @Test
    void auth_blank_generatesUuid() throws Exception {
        Main.scanner = new Scanner(new ByteArrayInputStream("\n".getBytes()));
        Method auth = getPrivateMethod("auth");
        auth.invoke(null);
        assertNotNull(Main.currentUserId, "UUID должен быть установлен");
    }

    // 4) auth с валидным UUID устанавливает его
    @Test
    void auth_withProvidedUuid_setsIt() throws Exception {
        UUID expected = UUID.randomUUID();
        String input = expected + "\n";
        Main.scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        Method auth = getPrivateMethod("auth");
        auth.invoke(null);
        assertEquals(expected, Main.currentUserId);
    }

    // 5) auth с НЕвалидным UUID -> IllegalArgumentException
    @Test
    void auth_withInvalidUuid_throws() throws Exception {
        Main.scanner = new Scanner(new ByteArrayInputStream("not-a-uuid\n".getBytes()));
        Method auth = getPrivateMethod("auth");
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> auth.invoke(null));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    // 6) Switch: ответ "y" вызывает auth() и меняет пользователя
    @Test
    void switch_yes_triggersAuthAndChangesUser() throws Exception {
        // исходный пользователь
        Main.currentUserId = UUID.randomUUID();

        // "y" -> заходим в auth(); там пустая строка -> создаст новый UUID
        Main.scanner = new Scanner(new ByteArrayInputStream("y\n\n".getBytes()));
        UUID before = Main.currentUserId;

        Method sw = getPrivateMethod("Switch");
        sw.invoke(null);

        UUID after = Main.currentUserId;
        assertNotNull(after);
        assertNotEquals(before, after, "После Switch('y') пользователь должен смениться");
    }

    // 7) Switch: ответ "n" не меняет пользователя
    @Test
    void switch_no_keepsUser() throws Exception {
        // из-за реализации: первый ввод игнорируется, второй должен быть "n"
        String input = "x\nn\n";
        Main.scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        UUID before = UUID.randomUUID();
        Main.currentUserId = before;

        Method sw = getPrivateMethod("Switch");
        sw.invoke(null);

        assertEquals(before, Main.currentUserId, "При Switch('n') пользователь не должен меняться");
    }

    // 8) isShort должен зависеть от domain: после смены домена поведение меняется
    @Test
    void isShort_respectsDomainChange() throws Exception {
        String customDomain = "https://short.local/";
        Main.domain = customDomain;

        Method isShort = getPrivateMethod("isShort", String.class);

        assertTrue((boolean) isShort.invoke(null, customDomain + "1234567890"));
        assertFalse((boolean) isShort.invoke(null, "https://another.local/abc"));
    }
}
