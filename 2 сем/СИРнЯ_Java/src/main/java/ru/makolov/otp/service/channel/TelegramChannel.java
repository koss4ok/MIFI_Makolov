package ru.makolov.otp.service.channel;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(TelegramChannel.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String chatId;
    private final String sendMessageUrl;

    public TelegramChannel() {
        Properties config = ChannelPropertiesLoader.loadOptional("telegram.properties");
        String token = readValue(config, "telegram.bot.token", "TELEGRAM_BOT_TOKEN");
        this.chatId = readValue(config, "telegram.chat.id", "TELEGRAM_CHAT_ID");

        String apiTemplate = config.getProperty("telegram.api.url", "https://api.telegram.org/bot%s/sendMessage");
        this.sendMessageUrl = String.format(apiTemplate, token);
    }

    @Override
    public String channel() {
        return "telegram";
    }

    @Override
    public void sendCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        String fullUrl = String.format("%s?chat_id=%s&text=%s",
                sendMessageUrl,
                urlEncode(chatId),
                urlEncode(message));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Telegram API returned status " + response.statusCode());
            }
            log.info("Telegram message sent successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Telegram request was interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Telegram request failed", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String readValue(Properties properties, String key, String envName) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required telegram property: " + key);
        }
        return value;
    }
}
