package com.nepalidate.ADBS.NepaliDateConverter;

public class CalendarDataUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CalendarDataUnavailableException(String message) {
        super(message);
    }
}
