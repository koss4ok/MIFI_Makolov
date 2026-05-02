package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import ru.makolov.otp.model.dto.RegisterRequest;
import ru.makolov.otp.service.AuthService;
import ru.makolov.otp.util.HttpResponseWriter;

public class AuthRegisterHandler extends BaseHandler {
    private final AuthService authService;

    public AuthRegisterHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "POST");
        RegisterRequest request = readBody(exchange, RegisterRequest.class);
        Map<String, Object> result = authService.register(request);
        HttpResponseWriter.writeSuccess(exchange, 201, result);
    }
}
