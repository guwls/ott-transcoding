package com.example.app.error;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String traceId,
        Map<String, Object> details
) {
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int status; private String code; private String message; private String traceId;
        private Map<String,Object> details;
        public Builder status(int s){ this.status=s; return this; }
        public Builder code(String c){ this.code=c; return this; }
        public Builder message(String m){ this.message=m; return this; }
        public Builder traceId(String t){ this.traceId=t; return this; }
        public Builder details(Map<String,Object> d){ this.details=d; return this; }
        public ErrorResponse build() {
            return new ErrorResponse(Instant.now(), status, code, message, traceId, details);
        }
    }
}