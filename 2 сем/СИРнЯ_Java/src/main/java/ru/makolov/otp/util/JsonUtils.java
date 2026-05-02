package ru.makolov.otp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public final class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static <T> T read(InputStream inputStream, Class<T> type) {
        try {
            return MAPPER.readValue(inputStream, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON body", e);
        }
    }

    public static byte[] write(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize response", e);
        }
    }
}
