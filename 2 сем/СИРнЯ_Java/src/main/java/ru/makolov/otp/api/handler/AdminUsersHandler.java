package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.security.JwtAuthService;
import ru.makolov.otp.service.AdminService;
import ru.makolov.otp.util.HttpResponseWriter;

public class AdminUsersHandler extends BaseHandler {
    private final AdminService adminService;
    private final JwtAuthService authService;

    public AdminUsersHandler(AdminService adminService, JwtAuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @Override
    protected void process(HttpExchange exchange) throws IOException {
        authService.requireAdmin(exchange.getRequestHeaders().getFirst("Authorization"));

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleList(exchange);
            return;
        }

        if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleDelete(exchange);
            return;
        }

        throw new ApiException(405, "METHOD_NOT_ALLOWED", "Allowed methods: GET, DELETE");
    }

    private void handleList(HttpExchange exchange) throws IOException {
        List<Map<String, Object>> users = adminService.listUsersWithoutAdmins();
        HttpResponseWriter.writeSuccess(exchange, 200, users);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/api/admin/users/";
        if (!path.startsWith(prefix) || path.length() <= prefix.length()) {
            throw new ApiException(400, "INVALID_PATH", "Use /api/admin/users/{id}");
        }
        String idPart = path.substring(prefix.length());
        long id;
        try {
            id = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "INVALID_USER_ID", "User id must be numeric");
        }

        Map<String, Object> result = adminService.deleteUser(id);
        HttpResponseWriter.writeSuccess(exchange, 200, result);
    }
}
