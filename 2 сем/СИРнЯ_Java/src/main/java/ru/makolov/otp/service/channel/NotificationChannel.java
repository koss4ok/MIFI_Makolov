package ru.makolov.otp.service.channel;

public interface NotificationChannel {
    String channel();

    void sendCode(String destination, String code);
}
