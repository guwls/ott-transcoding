package com.example.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "security")
public class SecurityProps {
    private List<String> apiKeys = List.of();
    public List<String> getApiKeys() { return apiKeys; }
    public void setApiKeys(List<String> apiKeys) { this.apiKeys = apiKeys; }
}