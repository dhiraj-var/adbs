package com.nepalidate.ADBS.NepaliDateConverter;

public class InvalidBsDayOfMonthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidBsDayOfMonthException(String message) {
        super(message);
    }
}
