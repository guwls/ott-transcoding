package com.example.app.error;

import io.swagger.v3.oas.annotations.media.*;

@Schema(name = "ErrorResponse")
public class ErrorSchemas {
    @Schema(description="Error response model", implementation = ErrorResponse.class)
    public static class ErrorResponseSchema {}
}
