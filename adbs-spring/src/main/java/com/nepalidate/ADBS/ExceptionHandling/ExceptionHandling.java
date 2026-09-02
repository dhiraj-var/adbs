package com.nepalidate.ADBS.ExceptionHandling;

import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.DateTimeException;
import java.util.Date;

/**
 * Global exception handler for adbs-core exceptions.
 *
 * Registered at the lowest priority so that if your application has its own
 * {@code @RestControllerAdvice}, yours will always take precedence over this one.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ErrResponse> invalidDateFormatException(
            InvalidDateFormatException ex, WebRequest request) {
        return bad(ex.getMessage(), request);
    }

    @ExceptionHandler(DateRangeNotSupported.class)
    public ResponseEntity<ErrResponse> invalidDateRangeException(
            DateRangeNotSupported ex, WebRequest request) {
        return bad(ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidBsDayOfMonthException.class)
    public ResponseEntity<ErrResponse> invalidBsDayOfMonthException(
            InvalidBsDayOfMonthException ex, WebRequest request) {
        return bad(ex.getMessage(), request);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ErrResponse> invalidDateTimeException(
            DateTimeException ex, WebRequest request) {
        return bad(ex.getMessage(), request);
    }

    private ResponseEntity<ErrResponse> bad(String message, WebRequest request) {
        ErrResponse body = new ErrResponse(
                HttpStatus.BAD_REQUEST.value(),
                new Date(),
                message,
                request.getDescription(false));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
