package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserStore {
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, String> users = new LinkedHashMap<>(); // login -> salt:hash

    public UserStore(String file) {
        this.file = Path.of(file);
        try {
            if (Files.exists(this.file)) {
                Map<String,String> m = mapper.readValue(Files.readString(this.file), new TypeReference<>(){});
                users.putAll(m);
            }
            else {
                Files.createDirectories(this.file.getParent());
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить пользователей: " + e.getMessage());
        }
    }

    public boolean exists(String login) {
        return users.containsKey(login);
    }

    public void save() {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(users);
            Files.writeString(file, json);
        }
        catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить пользователей", e);
        }
    }

    public void put(String login, String hash) {
        users.put(login, hash);
        save();
    }

    public String get(String login) {
        return users.get(login);
    }


    public static String hash(String password) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(password.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String password, String hash) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(password.getBytes());
            var currentHash = HexFormat.of().formatHex(hashBytes);
            return currentHash.equals(hash);
        } catch (Exception e) {
            return false;
        }
    }
}
