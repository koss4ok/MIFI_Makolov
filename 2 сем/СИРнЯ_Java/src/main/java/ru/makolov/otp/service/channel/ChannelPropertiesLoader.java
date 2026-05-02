package ru.makolov.otp.service.channel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ChannelPropertiesLoader {
    private ChannelPropertiesLoader() {
    }

    public static Properties loadOptional(String resourceName) {
        Properties properties = new Properties();
        try (InputStream inputStream = ChannelPropertiesLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load " + resourceName, e);
        }
        return properties;
    }
}
