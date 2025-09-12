package com.example.app.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import jakarta.servlet.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ErrorJsonWriter {
    private static final ObjectMapper om = new ObjectMapper();
    public static void write(HttpServletResponse w, int status, String code, String msg, Map<String,Object> details) {
        try {
            var body = ErrorResponse.builder()
                    .status(status).code(code).message(msg).traceId(MDC.get("traceId")).details(details).build();
            var json = om.writeValueAsString(body);
            w.setStatus(status);
            w.setContentType("application/json");
            w.setCharacterEncoding(StandardCharsets.UTF_8.name());
            w.getWriter().write(json);
        } catch (Exception ignored) {}
    }
}