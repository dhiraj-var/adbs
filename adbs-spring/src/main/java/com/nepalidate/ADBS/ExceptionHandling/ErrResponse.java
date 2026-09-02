package com.nepalidate.ADBS.ExceptionHandling;

import java.util.Date;

public class ErrResponse {

    private int statusCode;
    private Date timestamp;
    private String message;
    private String description;

    public ErrResponse(int statusCode, Date timestamp, String message, String description) {
        this.statusCode = statusCode;
        this.timestamp = new Date(timestamp.getTime());
        this.message = message;
        this.description = description;
    }

    public int getStatusCode() { return statusCode; }
    public Date getTimestamp() { return new Date(timestamp.getTime()); }
    public String getMessage() { return message; }
    public String getDescription() { return description; }
}
