package com.nepalidate.ADBS.NepaliDateConverter;

public class CalendarDataUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "CALENDAR_DATA_UNAVAILABLE";

    public CalendarDataUnavailableException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}
