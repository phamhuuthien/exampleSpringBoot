package com.example.learn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseData <T>{
    private int status;
    private String message;

//    khi data null không hiển thị
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;

    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResponseData(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
