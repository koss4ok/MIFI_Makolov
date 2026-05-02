package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import ru.makolov.otp.model.AuthContext;
import ru.makolov.otp.model.dto.OtpGenerateRequest;
import ru.makolov.otp.security.JwtAuthService;
import ru.makolov.otp.service.OtpService;
import ru.makolov.otp.util.HttpResponseWriter;

public class UserOtpGenerateHandler extends BaseHandler {
    private final OtpService otpService;
    private final JwtAuthService authService;

    public UserOtpGenerateHandler(OtpService otpService, JwtAuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        AuthContext authContext = authService.requireUser(exchange.getRequestHeaders().getFirst("Authorization"));
        OtpGenerateRequest request = readBody(exchange, OtpGenerateRequest.class);
        Map<String, Object> result = otpService.generate(authContext, request);
        HttpResponseWriter.writeSuccess(exchange, 201, result);
    }
}
