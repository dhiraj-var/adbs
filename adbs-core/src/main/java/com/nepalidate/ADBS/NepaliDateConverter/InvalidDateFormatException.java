package com.nepalidate.ADBS.NepaliDateConverter;

public class InvalidDateFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "INVALID_DATE_FORMAT";

    public InvalidDateFormatException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
