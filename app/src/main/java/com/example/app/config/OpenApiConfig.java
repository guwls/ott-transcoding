package com.example.app.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OTT Transcoding API")
                        .version("v1")
                        .description("Uploads, transcode enqueue, video details"));
    }
}
