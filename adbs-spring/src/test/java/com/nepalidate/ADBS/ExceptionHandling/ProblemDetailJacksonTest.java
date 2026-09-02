package com.nepalidate.ADBS.ExceptionHandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the RFC 7807 JSON shape is real, and that {@link ProblemDetail#getTimestamp()}
 * serializes as ISO-8601 given the setup this class's Javadoc says is required:
 * Jackson's {@code jackson-datatype-jsr310} module registered, and
 * {@code SerializationFeature.WRITE_DATES_AS_TIMESTAMPS} disabled (otherwise Jackson
 * writes {@code Instant} as a numeric epoch timestamp, not a string). Spring Boot's
 * web starter does both automatically; a plain Spring Framework app without Boot must
 * configure both itself.
 */
class ProblemDetailJacksonTest {

    @Test
    void serializesWithAllSevenFieldsAndIsoInstant() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ProblemDetail body = new ProblemDetail(
                "urn:adbs:problem:invalid-bs-day-of-month",
                "Invalid Bikram Sambat Day Of Month",
                422,
                "Day 32 is not valid for month 1 of Nepali year 2083. This month only has 31 days.",
                "/api/dates/convert",
                "INVALID_BS_DAY_OF_MONTH",
                Instant.parse("2026-09-02T10:15:30.123Z"));

        String json = mapper.writeValueAsString(body);

        assertTrue(json.contains("\"type\":\"urn:adbs:problem:invalid-bs-day-of-month\""));
        assertTrue(json.contains("\"title\":\"Invalid Bikram Sambat Day Of Month\""));
        assertTrue(json.contains("\"status\":422"));
        assertTrue(json.contains("\"detail\":\"Day 32 is not valid for month 1 of Nepali year 2083. This month only has 31 days.\""));
        assertTrue(json.contains("\"instance\":\"/api/dates/convert\""));
        assertTrue(json.contains("\"errorCode\":\"INVALID_BS_DAY_OF_MONTH\""));
        // ISO-8601 string, not a {"epochSecond":...,"nano":...} object.
        assertTrue(json.contains("\"timestamp\":\"2026-09-02T10:15:30.123Z\""));
    }
}
