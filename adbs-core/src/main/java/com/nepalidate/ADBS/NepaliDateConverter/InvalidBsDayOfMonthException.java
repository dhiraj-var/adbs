package com.nepalidate.ADBS.NepaliDateConverter;

public class InvalidBsDayOfMonthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "INVALID_BS_DAY_OF_MONTH";

    public InvalidBsDayOfMonthException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
