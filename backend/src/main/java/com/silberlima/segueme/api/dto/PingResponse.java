package com.silberlima.segueme.api.dto;

public class PingResponse {

    private final String message;

    public PingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
