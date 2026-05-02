package ru.makolov.otp.service.channel;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(SmsChannel.class);
    private final OutboxMessageWriter outboxMessageWriter;
    private final boolean enabled;
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsChannel(String outboxDir) {
        Properties config = ChannelPropertiesLoader.loadOptional("sms.properties");
        this.enabled = Boolean.parseBoolean(config.getProperty("sms.enabled", "false"));
        this.host = config.getProperty("smpp.host", "localhost");
        this.port = Integer.parseInt(config.getProperty("smpp.port", "2775"));
        this.systemId = config.getProperty("smpp.system_id", "smppclient1");
        this.password = config.getProperty("smpp.password", "password");
        this.systemType = config.getProperty("smpp.system_type", "OTP");
        this.sourceAddress = config.getProperty("smpp.source_addr", "OTPService");
        this.outboxMessageWriter = new OutboxMessageWriter(outboxDir, channel());

        if (!enabled) {
            log.info("SMS SMPP sending is disabled; file outbox emulation is active");
        }
    }

    @Override
    public String channel() {
        return "sms";
    }

    @Override
    public void sendCode(String destination, String code) {
        String message = "Your code: " + code;

        if (enabled) {
            try {
                sendBySmpp(destination, message);
                log.info("SMS was sent by SMPP emulator to {}", destination);
            } catch (Exception e) {
                log.error("SMPP send failed, fallback to file outbox for {}", destination, e);
            }
        }

        outboxMessageWriter.write(destination, message);
        log.info("SMS emulation copy was saved to outbox for {}", destination);
    }

    private void sendBySmpp(String destination, String message) throws Exception {
        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );

            session.connectAndBind(host, port, bindParameter);

            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    message.getBytes(StandardCharsets.UTF_8)
            );
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception ignored) {
            }
        }
    }
}
