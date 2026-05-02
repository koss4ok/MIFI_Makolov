package ru.makolov.otp;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.makolov.otp.api.handler.AdminConfigHandler;
import ru.makolov.otp.api.handler.AdminUsersHandler;
import ru.makolov.otp.api.handler.AuthLoginHandler;
import ru.makolov.otp.api.handler.AuthRegisterHandler;
import ru.makolov.otp.api.handler.StaticContentHandler;
import ru.makolov.otp.api.handler.UserOtpGenerateHandler;
import ru.makolov.otp.api.handler.UserOtpValidateHandler;
import ru.makolov.otp.config.AppConfig;
import ru.makolov.otp.config.DatabaseInitializer;
import ru.makolov.otp.dao.OtpCodeDao;
import ru.makolov.otp.dao.OtpConfigDao;
import ru.makolov.otp.dao.UserDao;
import ru.makolov.otp.dao.jdbc.JdbcOtpCodeDao;
import ru.makolov.otp.dao.jdbc.JdbcOtpConfigDao;
import ru.makolov.otp.dao.jdbc.JdbcUserDao;
import ru.makolov.otp.security.JwtAuthService;
import ru.makolov.otp.security.JwtTokenService;
import ru.makolov.otp.service.AdminService;
import ru.makolov.otp.service.AuthService;
import ru.makolov.otp.service.OtpService;
import ru.makolov.otp.service.channel.EmailChannel;
import ru.makolov.otp.service.channel.FileChannelStub;
import ru.makolov.otp.service.channel.NotificationChannel;
import ru.makolov.otp.service.channel.SmsChannel;
import ru.makolov.otp.service.channel.TelegramChannel;
import ru.makolov.otp.service.impl.AdminServiceJdbc;
import ru.makolov.otp.service.impl.AuthServiceJdbc;
import ru.makolov.otp.service.impl.OtpServiceJdbc;
import ru.makolov.otp.service.stub.OtpExpirationScheduler;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        new DatabaseInitializer(config).initialize();

        UserDao userDao = new JdbcUserDao(config.dbUrl(), config.dbUser(), config.dbPassword());
        OtpConfigDao otpConfigDao = new JdbcOtpConfigDao(config.dbUrl(), config.dbUser(), config.dbPassword());
        OtpCodeDao otpCodeDao = new JdbcOtpCodeDao(config.dbUrl(), config.dbUser(), config.dbPassword());
        JwtTokenService jwtTokenService = new JwtTokenService(config.jwtSecret(), config.jwtTtlSeconds());
        JwtAuthService jwtAuthService = new JwtAuthService(jwtTokenService);

        NotificationChannel emailChannel = new EmailChannel(config.outboxDir());
        NotificationChannel smsChannel = new SmsChannel(config.outboxDir());
        NotificationChannel telegramChannel = new TelegramChannel();
        NotificationChannel fileChannel = new FileChannelStub(config.fileChannelPath());

        AuthService authService = new AuthServiceJdbc(userDao, jwtTokenService);
        AdminService adminService = new AdminServiceJdbc(userDao, otpConfigDao);
        OtpService otpService = new OtpServiceJdbc(otpCodeDao, otpConfigDao, emailChannel, smsChannel, telegramChannel, fileChannel);
        OtpExpirationScheduler scheduler = new OtpExpirationScheduler(otpCodeDao);

        HttpServer server = HttpServer.create(new InetSocketAddress(config.serverPort()), 0);
        server.createContext("/api/auth/register", new AuthRegisterHandler(authService));
        server.createContext("/api/auth/login", new AuthLoginHandler(authService));
        server.createContext("/api/user/otp/generate", new UserOtpGenerateHandler(otpService, jwtAuthService));
        server.createContext("/api/user/otp/validate", new UserOtpValidateHandler(otpService, jwtAuthService));
        server.createContext("/api/admin/config", new AdminConfigHandler(adminService, jwtAuthService));
        server.createContext("/api/admin/users", new AdminUsersHandler(adminService, jwtAuthService));
        server.createContext("/", new StaticContentHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        scheduler.start();

        log.info("MAKOLOV-otp started on port {}", config.serverPort());
    }
}
