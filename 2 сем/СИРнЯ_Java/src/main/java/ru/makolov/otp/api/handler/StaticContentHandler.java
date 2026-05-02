package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class StaticContentHandler implements HttpHandler {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".html", "text/html; charset=utf-8",
            ".css", "text/css; charset=utf-8",
            ".js", "application/javascript; charset=utf-8",
            ".json", "application/json; charset=utf-8"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            path = "/index.html";
        }

        if (path.contains("..")) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }

        String resourcePath = "web" + path;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            byte[] body = inputStream.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", detectContentType(path));
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }
    }

    private static String detectContentType(String path) {
        for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
            if (path.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "application/octet-stream";
    }
}
