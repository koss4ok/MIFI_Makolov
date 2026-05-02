package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import ru.makolov.otp.model.AuthContext;
import ru.makolov.otp.model.dto.OtpValidateRequest;
import ru.makolov.otp.security.JwtAuthService;
import ru.makolov.otp.service.OtpService;
import ru.makolov.otp.util.HttpResponseWriter;

public class UserOtpValidateHandler extends BaseHandler {
    private final OtpService otpService;
    private final JwtAuthService authService;

    public UserOtpValidateHandler(OtpService otpService, JwtAuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        AuthContext authContext = authService.requireUser(exchange.getRequestHeaders().getFirst("Authorization"));
        OtpValidateRequest request = readBody(exchange, OtpValidateRequest.class);
        Map<String, Object> result = otpService.validate(authContext, request);
        HttpResponseWriter.writeSuccess(exchange, 200, result);
    }
}
