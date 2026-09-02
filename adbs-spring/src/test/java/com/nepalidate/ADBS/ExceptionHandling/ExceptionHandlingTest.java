package com.nepalidate.ADBS.ExceptionHandling;

import com.nepalidate.ADBS.NepaliDateConverter.CalendarDataUnavailableException;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionHandlingTest {

    private final ExceptionHandling handler = new ExceptionHandling();

    private static WebRequest requestFor(String path) {
        return new ServletWebRequest(new MockHttpServletRequest("GET", path));
    }

    private static void assertProblem(ResponseEntity<ProblemDetail> response, HttpStatus expectedStatus,
            String expectedType, String expectedTitle, String expectedErrorCode, String expectedDetail,
            Instant before) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());

        ProblemDetail body = response.getBody();
        assertNotNull(body);
        assertEquals(expectedType, body.getType());
        assertEquals(expectedTitle, body.getTitle());
        assertEquals(expectedStatus.value(), body.getStatus());
        assertEquals(expectedDetail, body.getDetail());
        assertEquals("/some/path", body.getInstance());
        assertEquals(expectedErrorCode, body.getErrorCode());

        assertNotNull(body.getTimestamp());
        Instant after = Instant.now();
        assertTrue(!body.getTimestamp().isBefore(before) && !body.getTimestamp().isAfter(after),
                "timestamp should fall between the call's before/after bounds");
    }

    @Test
    void invalidDateFormatException_returns400() {
        Instant before = Instant.now();
        InvalidDateFormatException ex = new InvalidDateFormatException("bad format");
        ResponseEntity<ProblemDetail> response = handler.invalidDateFormatException(ex, requestFor("/some/path"));

        assertProblem(response, HttpStatus.BAD_REQUEST, "urn:adbs:problem:invalid-date-format",
                "Invalid Date Format", "INVALID_DATE_FORMAT", "bad format", before);
    }

    @Test
    void dateRangeNotSupported_returns422() {
        Instant before = Instant.now();
        DateRangeNotSupported ex = new DateRangeNotSupported("out of range");
        ResponseEntity<ProblemDetail> response = handler.invalidDateRangeException(ex, requestFor("/some/path"));

        assertProblem(response, HttpStatus.UNPROCESSABLE_ENTITY, "urn:adbs:problem:date-range-not-supported",
                "Date Range Not Supported", "DATE_RANGE_NOT_SUPPORTED", "out of range", before);
    }

    @Test
    void invalidBsDayOfMonthException_returns422() {
        Instant before = Instant.now();
        InvalidBsDayOfMonthException ex = new InvalidBsDayOfMonthException("bad day");
        ResponseEntity<ProblemDetail> response = handler.invalidBsDayOfMonthException(ex, requestFor("/some/path"));

        assertProblem(response, HttpStatus.UNPROCESSABLE_ENTITY, "urn:adbs:problem:invalid-bs-day-of-month",
                "Invalid Bikram Sambat Day Of Month", "INVALID_BS_DAY_OF_MONTH", "bad day", before);
    }

    @Test
    void dateTimeException_returns400() {
        Instant before = Instant.now();
        DateTimeException ex = new DateTimeException("bad date/time");
        ResponseEntity<ProblemDetail> response = handler.invalidDateTimeException(ex, requestFor("/some/path"));

        assertProblem(response, HttpStatus.BAD_REQUEST, "urn:adbs:problem:invalid-date-format",
                "Invalid Date Format", "INVALID_DATE_FORMAT", "bad date/time", before);
    }

    @Test
    void calendarDataUnavailableException_returns500() {
        Instant before = Instant.now();
        CalendarDataUnavailableException ex = new CalendarDataUnavailableException("csv missing");
        ResponseEntity<ProblemDetail> response = handler.calendarDataUnavailableException(ex, requestFor("/some/path"));

        assertProblem(response, HttpStatus.INTERNAL_SERVER_ERROR, "urn:adbs:problem:calendar-data-unavailable",
                "Calendar Data Unavailable", "CALENDAR_DATA_UNAVAILABLE", "csv missing", before);
    }

    @Test
    void instance_stripsUriPrefixRegardlessOfPath() {
        InvalidDateFormatException ex = new InvalidDateFormatException("bad format");
        ResponseEntity<ProblemDetail> response =
                handler.invalidDateFormatException(ex, requestFor("/api/dates/convert"));

        assertEquals("/api/dates/convert", response.getBody().getInstance());
    }
}
