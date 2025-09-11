package com.example.app.config;

import com.example.app.ratelimit.EnqueueRateLimitFilter;
import com.example.app.security.ApiKeyAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyFilter(ApiKeyAuthFilter f) {
        var reg = new FilterRegistrationBean<>(f);
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<EnqueueRateLimitFilter> rateLimitFilter(EnqueueRateLimitFilter f) {
        var reg = new FilterRegistrationBean<>(f);
        reg.setOrder(2);
        return reg;
    }
}