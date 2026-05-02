package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;
import ru.makolov.otp.model.dto.OtpConfigRequest;
import ru.makolov.otp.security.JwtAuthService;
import ru.makolov.otp.service.AdminService;
import ru.makolov.otp.util.HttpResponseWriter;

public class AdminConfigHandler extends BaseHandler {
    private final AdminService adminService;
    private final JwtAuthService authService;

    public AdminConfigHandler(AdminService adminService, JwtAuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        requireMethod(exchange, "PUT");
        authService.requireAdmin(exchange.getRequestHeaders().getFirst("Authorization"));
        OtpConfigRequest request = readBody(exchange, OtpConfigRequest.class);
        Map<String, Object> result = adminService.updateConfig(request);
        HttpResponseWriter.writeSuccess(exchange, 200, result);
    }
}
