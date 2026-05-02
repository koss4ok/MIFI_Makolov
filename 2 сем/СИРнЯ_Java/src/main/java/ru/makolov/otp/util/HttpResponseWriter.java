package ru.makolov.otp.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpResponseWriter {
    private HttpResponseWriter() {
    }

    public static void writeSuccess(HttpExchange exchange, int status, Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        body.put("error", null);
        write(exchange, status, body);
    }

    public static void writeError(HttpExchange exchange, int status, String code, String message) throws IOException {
        Map<String, Object> error = Map.of(
                "code", code,
                "message", message
        );
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("data", null);
        body.put("error", error);
        write(exchange, status, body);
    }

    public static void write(HttpExchange exchange, int status, Object bodyObject) throws IOException {
        byte[] body = JsonUtils.write(bodyObject);
        exchange.setAttribute("responseStatus", status);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
