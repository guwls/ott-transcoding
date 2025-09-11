package com.example.app.ratelimit;

import com.example.app.security.ApiKeyAuthFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EnqueueRateLimitFilter implements Filter {

    private final TokenBucketRateLimiter limiter;

    public EnqueueRateLimitFilter(TokenBucketRateLimiter limiter) { this.limiter = limiter; }

    private boolean isEnqueuePath(HttpServletRequest r) {
        if (!"POST".equalsIgnoreCase(r.getMethod())) return false;
        // /videos/{id}/enqueue-transcode 간단 매칭
        String p = r.getRequestURI();
        return p != null && p.matches("^/videos/\\d+/enqueue-transcode$");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        var r = (HttpServletRequest) req;
        var w = (HttpServletResponse) res;

        if (!isEnqueuePath(r)) { chain.doFilter(req, res); return; }

        String user = (String) r.getAttribute(ApiKeyAuthFilter.ATTR_USER);
        String bucketKey = "rl:user:" + user + ":enqueue";
        var v = limiter.allow(bucketKey);

        // 공통 헤더
        w.setHeader("X-RateLimit-Limit",     String.valueOf(limiter.capacity()));
        w.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, v.remaining())));
        if (!v.allowed()) {
            long retrySec = (long) Math.ceil(v.retryAfterMs() / 1000.0);
            w.setHeader("Retry-After", String.valueOf(retrySec));
            w.setStatus(429);
            w.setContentType("application/json");
            w.getWriter().write("{\"status\":429,\"code\":\"RATE_LIMITED\",\"message\":\"too many requests\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}