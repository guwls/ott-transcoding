package com.example.app.error;

public class AppException extends RuntimeException {
    private final String code; private final int httpStatus;
    public AppException(String code, int httpStatus, String message) {
        super(message); this.code=code; this.httpStatus=httpStatus;
    }
    public String code(){ return code; } public int httpStatus(){ return httpStatus; }
    public static AppException badRequest(String code, String msg){ return new AppException(code, 400, msg); }
    public static AppException notFound(String code, String msg){ return new AppException(code, 404, msg); }
    public static AppException conflict(String code, String msg){ return new AppException(code, 409, msg); }
}
