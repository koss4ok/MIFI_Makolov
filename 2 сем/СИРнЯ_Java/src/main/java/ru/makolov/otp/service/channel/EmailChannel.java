package ru.makolov.otp.service.channel;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(EmailChannel.class);
    private static final String DEFAULT_SUBJECT = "Your OTP Code";

    private final boolean enabled;
    private final String fromEmail;
    private final Session session;
    private final OutboxMessageWriter outboxMessageWriter;

    public EmailChannel(String outboxDir) {
        Properties config = ChannelPropertiesLoader.loadOptional("email.properties");
        this.enabled = Boolean.parseBoolean(config.getProperty("email.enabled", "false"));
        this.fromEmail = config.getProperty("email.from", "noreply@example.com");
        this.outboxMessageWriter = new OutboxMessageWriter(outboxDir, channel());

        if (!enabled) {
            this.session = null;
            log.info("Email channel real sending is disabled; file outbox is active");
            return;
        }

        String username = readValue(config, "email.username", "EMAIL_USERNAME");
        String password = readValue(config, "email.password", "EMAIL_PASSWORD");
        this.session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public String channel() {
        return "email";
    }

    @Override
    public void sendCode(String destination, String code) {
        String messageText = "Your verification code is: " + code;
        outboxMessageWriter.write(destination, messageText);

        if (!enabled) {
            log.info("Email stub copy was saved to outbox for {}", destination);
            return;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject(DEFAULT_SUBJECT);
            message.setText(messageText);
            Transport.send(message);
            log.info("Email was sent to {}", destination);
        } catch (Exception e) {
            log.error("Email sending failed for {}", destination, e);
        }
    }

    private static String readValue(Properties properties, String key, String envName) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required email property: " + key);
        }
        return value;
    }
}
