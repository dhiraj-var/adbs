package com.nepalidate.ADBS.ExceptionHandling;

import java.time.Instant;

/**
 * RFC 7807 ("Problem Details for HTTP APIs") error response body. {@code errorCode}
 * is an RFC 7807 extension member for callers who want to branch on a stable
 * machine-readable code instead of parsing {@code detail} text.
 *
 * <p>Serializing {@code timestamp} as an ISO-8601 string (rather than a numeric
 * epoch timestamp) requires Jackson's {@code jackson-datatype-jsr310} module to
 * be registered, and {@code SerializationFeature.WRITE_DATES_AS_TIMESTAMPS} to
 * be disabled. Spring Boot's web starter does both automatically; a plain
 * Spring Framework app without Boot must configure both itself.
 */
public class ProblemDetail {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final String errorCode;
    private final Instant timestamp;

    public ProblemDetail(String type, String title, int status, String detail,
            String instance, String errorCode, Instant timestamp) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.errorCode = errorCode;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
    public String getDetail() { return detail; }
    public String getInstance() { return instance; }
    public String getErrorCode() { return errorCode; }
    public Instant getTimestamp() { return timestamp; }
}
