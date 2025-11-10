package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersistenceService {
    private final Path dataDir;
    private final ObjectMapper mapper;

    public PersistenceService(String dataDir) {
        this.dataDir = Path.of(dataDir);
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Files.createDirectories(this.dataDir);
        }
        catch (IOException ignored) {}
    }

    public Wallet loadWallet(String username) {
        Path f = dataDir.resolve(username + "-wallet.json");
        if (Files.exists(f)) {
            try {
                return mapper.readValue(Files.readString(f), Wallet.class);
            } catch (Exception e) {
                System.err.println("Не удалось загрузить кошелёк, создан новый: " + e.getMessage());
            }
        }
        return new Wallet();
    }

    public void saveWallet(String username, Wallet wallet) throws IOException {
        Path f = dataDir.resolve(username + "-wallet.json");
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wallet);
        Files.writeString(f, json);
    }
}
