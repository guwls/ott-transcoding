package com.example.app.security;

import com.example.app.config.SecurityProps;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class ApiKeyAuthFilter implements Filter {
    private final Set<String> whitelist;
    public static final String ATTR_USER = "X-User";
    public static final String HEADER    = "X-Api-Key";

    public ApiKeyAuthFilter(SecurityProps props) {
        var keys = props.getApiKeys();
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("security.api-keys is missing or empty");
        }
        this.whitelist = Set.copyOf(keys);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var r = (HttpServletRequest) req;
        var w = (HttpServletResponse) res;
        String key = r.getHeader(HEADER);

        if (key == null || key.isBlank() || !whitelist.contains(key)) {
            w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            w.setContentType("application/json");
            w.getWriter().write("{\"status\":401,\"code\":\"UNAUTHORIZED\",\"message\":\"invalid api key\"}");
            return;
        }

        r.setAttribute(ATTR_USER, key);
        MDC.put("user", key);
        try { chain.doFilter(req, res); } finally { MDC.remove("user"); }
    }
}