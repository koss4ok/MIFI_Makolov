package ru.makolov.otp.service.channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class OutboxMessageWriter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
            .withZone(ZoneOffset.UTC);
    private final Path channelDir;

    public OutboxMessageWriter(String rootDir, String channelName) {
        this.channelDir = Path.of(rootDir, channelName);
    }

    public void write(String destination, String message) {
        try {
            Files.createDirectories(channelDir);
            String fileName = FORMATTER.format(Instant.now()) + "-" + UUID.randomUUID() + ".txt";
            Path filePath = channelDir.resolve(fileName);
            String content = "destination=" + destination + System.lineSeparator()
                    + "message=" + message + System.lineSeparator();
            Files.writeString(filePath, content);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write message to outbox", e);
        }
    }
}
