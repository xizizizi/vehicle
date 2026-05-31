package io.vehicle.vehicle_admin.exception;

public class CozeApiException extends RuntimeException {

    public CozeApiException(String message) {
        super(message);
    }

    public CozeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}