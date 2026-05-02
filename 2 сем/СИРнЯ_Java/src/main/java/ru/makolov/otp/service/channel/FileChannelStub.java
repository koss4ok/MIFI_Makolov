package ru.makolov.otp.service.channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChannelStub implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(FileChannelStub.class);
    private final Path path;

    public FileChannelStub(String filePath) {
        this.path = Path.of(filePath);
    }

    @Override
    public String channel() {
        return "file";
    }

    @Override
    public void sendCode(String destination, String code) {
        String line = "destination=" + destination + ", code=" + code + System.lineSeparator();
        try {
            Files.writeString(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("File stub: code saved to {}", path.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write otp code to file", e);
        }
    }
}
