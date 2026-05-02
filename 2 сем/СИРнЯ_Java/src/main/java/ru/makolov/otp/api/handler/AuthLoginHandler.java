package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import ru.makolov.otp.model.dto.LoginRequest;
import ru.makolov.otp.service.AuthService;
import ru.makolov.otp.util.HttpResponseWriter;

public class AuthLoginHandler extends BaseHandler {
    private final AuthService authService;

    public AuthLoginHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        LoginRequest request = readBody(exchange, LoginRequest.class);
        Map<String, Object> result = authService.login(request);
        HttpResponseWriter.writeSuccess(exchange, 200, result);
    }
}
