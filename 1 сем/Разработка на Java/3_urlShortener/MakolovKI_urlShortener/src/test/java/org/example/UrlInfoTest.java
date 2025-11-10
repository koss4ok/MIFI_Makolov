package org.example;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UrlInfoTest {

    private Path originalCsvPath;
    private Path testCsvPath;

    @BeforeEach
    void setUp() throws IOException {
        originalCsvPath = Main.csvPath;
        testCsvPath = Path.of("./DB_test.csv");
        Main.csvPath = testCsvPath;

        // чистый файл перед каждым тестом
        Files.deleteIfExists(testCsvPath);
        Files.writeString(testCsvPath, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @AfterEach
    void tearDown() throws IOException {
        Main.csvPath = originalCsvPath;
        Files.deleteIfExists(testCsvPath);
    }

    // 1) Создание новой ссылки пишет строку и корректно форматирует ShortUrl
    @Test
    void createNewUrl_writesLineAndShortUrlMatchesFactory() throws Exception {
        UUID author = UUID.randomUUID();
        String full = "https://example.com/a?b=c";
        new UrlInfo(full, author, 2, 5);

        List<UrlInfo> all = UrlInfo.GetAllInfo();
        assertEquals(1, all.size());

        UrlInfo stored = all.get(0);
        assertEquals(full, stored.FullUrl);
        assertEquals(author, stored.AuthorId);
        assertEquals(5, stored.Limit);
        assertEquals(0, stored.LinkOpens);
        assertTrue(stored.ValidUntil.isAfter(LocalDateTime.now()));

        String expectedShort = UrlInfo.createShortUrl(full, author);
        assertEquals(expectedShort, stored.ShortUrl);
        assertTrue(stored.ShortUrl.startsWith(Main.domain));
        assertEquals(Main.domain.length() + 10, stored.ShortUrl.length());
    }

    // 2) TryGetUrl возвращает ссылку, если она валидна
    @Test
    void tryGetUrl_returnsUrl_whenValid() throws Exception {
        UUID author = UUID.randomUUID();
        UrlInfo ui = new UrlInfo("http://site.ru/x", author, 3, 10);

        UrlInfo found = UrlInfo.TryGetUrl(author, ui.ShortUrl);
        assertNotNull(found);
        assertEquals(ui.ShortUrl, found.ShortUrl);
        assertEquals(ui.FullUrl, found.FullUrl);
    }

    // 3) TryGetUrl возвращает null и удаляет запись, если лимит исчерпан (Limit <= LinkOpens)
    @Test
    void tryGetUrl_returnsNull_whenLimitReached_andDeletes() throws Exception {
        UUID author = UUID.randomUUID();
        String shortUrl = UrlInfo.createShortUrl("http://site.ru/limit", author);

        String line = String.join(";",
                "http://site.ru/limit",
                shortUrl,
                author.toString(),
                LocalDateTime.now().plusDays(1).toString(),
                "1",
                "1") + "\r\n";
        Files.writeString(Main.csvPath, line, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        UrlInfo result = UrlInfo.TryGetUrl(author, shortUrl);
        assertNull(result, "Должно быть null при исчерпанном лимите");

        String content = Files.readString(Main.csvPath);
        assertTrue(content.isEmpty(), "Запись должна быть удалена из файла");
    }

    // 4) TryGetUrl возвращает null и удаляет запись, если срок жизни истёк
    @Test
    void tryGetUrl_returnsNull_whenExpired_andDeletes() throws Exception {
        UUID author = UUID.randomUUID();
        String shortUrl = UrlInfo.createShortUrl("http://site.ru/expired", author);

        String line = String.join(";",
                "http://site.ru/expired",
                shortUrl,
                author.toString(),
                LocalDateTime.now().minusDays(1).toString(),
                "5",
                "0") + "\r\n";
        Files.writeString(Main.csvPath, line, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        UrlInfo result = UrlInfo.TryGetUrl(author, shortUrl);
        assertNull(result, "Должно быть null при истёкшем сроке");

        String content = Files.readString(Main.csvPath);
        assertTrue(content.isEmpty(), "Просроченная запись должна быть удалена из файла");
    }

    // 5) createShortUrl детерминированная: одинаковый ввод -> одинаковый вывод
    @Test
    void createShortUrl_isDeterministic() throws Exception {
        UUID author = UUID.randomUUID();
        String full = "https://example.org/page";
        String a = UrlInfo.createShortUrl(full, author);
        String b = UrlInfo.createShortUrl(full, author);
        assertEquals(a, b);
    }

    // 6) createShortUrl различается при разном пользователе
    @Test
    void createShortUrl_diffUserGivesDifferentHash() throws Exception {
        String full = "https://example.org/page";
        String s1 = UrlInfo.createShortUrl(full, UUID.randomUUID());
        String s2 = UrlInfo.createShortUrl(full, UUID.randomUUID());
        assertNotEquals(s1, s2);
    }

    // 7) createShortUrl различается при разном полном URL
    @Test
    void createShortUrl_diffUrlGivesDifferentHash() throws Exception {
        UUID author = UUID.randomUUID();
        String s1 = UrlInfo.createShortUrl("https://a.example/x", author);
        String s2 = UrlInfo.createShortUrl("https://a.example/y", author);
        assertNotEquals(s1, s2);
    }

    // 8) GetAllInfo читает несколько строк
    @Test
    void getAllInfo_readsMultiple() throws Exception {
        UUID a1 = UUID.randomUUID();
        UUID a2 = UUID.randomUUID();
        new UrlInfo("http://host/1", a1, 5, 9);
        new UrlInfo("http://host/2", a2, 5, 9);

        List<UrlInfo> list = UrlInfo.GetAllInfo();
        assertEquals(2, list.size());
    }

    // 9) Конструктор добавляет записи в конец (append)
    @Test
    void constructor_appendsRecords() throws Exception {
        UUID a = UUID.randomUUID();
        new UrlInfo("http://h/1", a, 1, 1);
        new UrlInfo("http://h/2", a, 1, 1);

        List<String> lines = Files.readAllLines(Main.csvPath);
        assertEquals(2, lines.stream().filter(s -> !s.isBlank()).count());
        assertTrue(lines.get(0).startsWith("http://h/1;"));
        assertTrue(lines.get(1).startsWith("http://h/2;"));
    }

}
