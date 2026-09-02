package com.nepalidate.ADBS.ExceptionHandling;

import com.nepalidate.ADBS.NepaliDateConverter.CalendarDataUnavailableException;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.DateTimeException;
import java.time.Instant;

/**
 * Global exception handler for adbs-core exceptions. Responds with RFC 7807
 * ("Problem Details for HTTP APIs") {@code application/problem+json} bodies.
 *
 * Registered at the lowest priority so that if your application has its own
 * {@code @RestControllerAdvice}, yours will always take precedence over this one.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ProblemDetail> invalidDateFormatException(
            InvalidDateFormatException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "urn:adbs:problem:invalid-date-format",
                "Invalid Date Format", ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DateRangeNotSupported.class)
    public ResponseEntity<ProblemDetail> invalidDateRangeException(
            DateRangeNotSupported ex, WebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "urn:adbs:problem:date-range-not-supported",
                "Date Range Not Supported", ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidBsDayOfMonthException.class)
    public ResponseEntity<ProblemDetail> invalidBsDayOfMonthException(
            InvalidBsDayOfMonthException ex, WebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "urn:adbs:problem:invalid-bs-day-of-month",
                "Invalid Bikram Sambat Day Of Month", ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ProblemDetail> invalidDateTimeException(
            DateTimeException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "urn:adbs:problem:invalid-date-format",
                "Invalid Date Format", "INVALID_DATE_FORMAT", ex.getMessage(), request);
    }

    @ExceptionHandler(CalendarDataUnavailableException.class)
    public ResponseEntity<ProblemDetail> calendarDataUnavailableException(
            CalendarDataUnavailableException ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "urn:adbs:problem:calendar-data-unavailable",
                "Calendar Data Unavailable", ex.getErrorCode(), ex.getMessage(), request);
    }

    private ResponseEntity<ProblemDetail> build(HttpStatus status, String type, String title,
            String errorCode, String detail, WebRequest request) {
        ProblemDetail body = new ProblemDetail(
                type, title, status.value(), detail, extractUri(request), errorCode, Instant.now());
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private String extractUri(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }
}
