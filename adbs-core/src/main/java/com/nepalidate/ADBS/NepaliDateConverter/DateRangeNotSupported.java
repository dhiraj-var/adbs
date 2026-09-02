package com.nepalidate.ADBS.NepaliDateConverter;

public class DateRangeNotSupported extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "DATE_RANGE_NOT_SUPPORTED";

    public DateRangeNotSupported(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
