package com.zjgsu.zxy.carbonservice.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
