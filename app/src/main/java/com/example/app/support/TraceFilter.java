package com.example.app.support;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceFilter implements Filter {
    public static final String HEADER = "X-Trace-Id";
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        var httpReq = (HttpServletRequest) req;
        var httpRes = (HttpServletResponse) res;
        String traceId = httpReq.getHeader(HEADER);
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            httpRes.setHeader(HEADER, traceId);
            chain.doFilter(req, res);
        } finally {
            MDC.remove("traceId");
        }
    }
}
