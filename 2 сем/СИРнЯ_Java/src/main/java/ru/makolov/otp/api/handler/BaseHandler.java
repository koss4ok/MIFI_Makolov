package ru.makolov.otp.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.makolov.otp.api.ApiException;
import ru.makolov.otp.util.HttpResponseWriter;
import ru.makolov.otp.util.JsonUtils;

public abstract class BaseHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseHandler.class);

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        long startedAt = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        log.info("API request: method={}, path={}, remote={}", method, path, exchange.getRemoteAddress());

        try {
            process(exchange);
        } catch (ApiException e) {
            HttpResponseWriter.writeError(exchange, e.status(), e.code(), e.getMessage());
        } catch (IllegalArgumentException e) {
            HttpResponseWriter.writeError(exchange, 400, "BAD_REQUEST", e.getMessage());
        } catch (Exception e) {
            HttpResponseWriter.writeError(exchange, 500, "INTERNAL_ERROR", "Unexpected server error");
        } finally {
            Object status = exchange.getAttribute("responseStatus");
            int responseStatus = status instanceof Integer i ? i : 500;
            long elapsed = System.currentTimeMillis() - startedAt;
            log.info("API response: method={}, path={}, status={}, elapsedMs={}", method, path, responseStatus, elapsed);
        }
    }

    protected abstract void process(HttpExchange exchange) throws IOException;

    protected void requireMethod(HttpExchange exchange, String method) {
        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
            throw new ApiException(405, "METHOD_NOT_ALLOWED", "Expected method " + method);
        }
    }

    protected <T> T readBody(HttpExchange exchange, Class<T> type) {
        return JsonUtils.read(exchange.getRequestBody(), type);
    }
}
