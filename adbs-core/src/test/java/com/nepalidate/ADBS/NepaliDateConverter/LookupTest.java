package com.nepalidate.ADBS.NepaliDateConverter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises {@link Lookup} directly, including the malformed-CSV paths that
 * {@code Lookup.load(InputStream)} exists specifically to make testable
 * without touching the real classpath resource.
 */
class LookupTest {

    @AfterEach
    void resetSharedState() {
        // Any load() call in a test (valid or not) must not leak into other
        // test classes sharing this JVM — force the next real usage to reload
        // from the actual classpath CSV.
        Lookup.resetForTesting();
    }

    private static ByteArrayInputStream streamOf(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }

    /** A synthetic but fully valid dataset spanning the whole supported range. */
    private static String fullValidCsv() {
        StringBuilder sb = new StringBuilder();
        LocalDate date = LocalDate.of(1900, 1, 1);
        for (int year = Lookup.START_YEAR; year <= Lookup.END_YEAR; year++) {
            sb.append(year).append(',').append(date)
              .append(",30,30,30,30,30,30,30,30,30,30,30,30\n");
            date = date.plusDays(360);
        }
        return sb.toString();
    }

    @Test
    void realCsv_firstAndLastYearMatchExpectedData() {
        assertEquals(LocalDate.of(1913, 4, 13), Lookup.newYearDate(0));
        assertEquals(LocalDate.of(2043, 4, 14), Lookup.newYearDate(Lookup.YEAR_COUNT - 1));
    }

    @Test
    void realCsv_everyMonthDayCountIsInValidRange() {
        for (int i = 0; i < Lookup.YEAR_COUNT; i++) {
            for (int m = 0; m < 12; m++) {
                int days = Lookup.monthDayCount(i, m);
                assertTrue(days >= 29 && days <= 32,
                        "year index " + i + " month " + m + " has invalid day count " + days);
            }
        }
    }

    @Test
    void syntheticFullCsv_loadsSuccessfully() throws IOException {
        Lookup.load(streamOf(fullValidCsv()));
        assertEquals(LocalDate.of(1900, 1, 1), Lookup.newYearDate(0));
        assertEquals(30, Lookup.monthDayCount(0, 0));
    }

    @Test
    void emptyStream_throwsCalendarDataUnavailable() {
        assertThrows(CalendarDataUnavailableException.class, () -> Lookup.load(streamOf("")));
    }

    @Test
    void wrongColumnCount_throwsCalendarDataUnavailable() {
        String csv = fullValidCsv().replaceFirst(",30,30,30,30,30,30,30,30,30,30,30,30", ",30,30");
        CalendarDataUnavailableException ex = assertThrows(CalendarDataUnavailableException.class,
                () -> Lookup.load(streamOf(csv)));
        assertTrue(ex.getMessage().contains("line 1"));
    }

    @Test
    void yearOutsideSupportedRange_throwsCalendarDataUnavailable() {
        String csv = (Lookup.START_YEAR - 1) + ",1900-01-01,30,30,30,30,30,30,30,30,30,30,30,30\n"
                + fullValidCsv();
        assertThrows(CalendarDataUnavailableException.class, () -> Lookup.load(streamOf(csv)));
    }

    @Test
    void monthDayCountOutsideValidRange_throwsCalendarDataUnavailable() {
        String csv = fullValidCsv().replaceFirst(",30,30,30,30,30,30,30,30,30,30,30,30",
                ",33,30,30,30,30,30,30,30,30,30,30,30");
        assertThrows(CalendarDataUnavailableException.class, () -> Lookup.load(streamOf(csv)));
    }

    @Test
    void missingYear_throwsCalendarDataUnavailable() {
        String[] lines = fullValidCsv().split("\n");
        StringBuilder csv = new StringBuilder();
        for (int i = 1; i < lines.length; i++) { // drop the first year's row, creating a gap
            csv.append(lines[i]).append('\n');
        }
        CalendarDataUnavailableException ex = assertThrows(CalendarDataUnavailableException.class,
                () -> Lookup.load(streamOf(csv.toString())));
        assertTrue(ex.getMessage().contains("missing data for BS year " + Lookup.START_YEAR));
    }

    @Test
    void failedLoad_doesNotPreventASubsequentSuccessfulLoad() {
        assertThrows(CalendarDataUnavailableException.class, () -> Lookup.load(streamOf("")));

        assertDoesNotThrow(() -> Lookup.load(streamOf(fullValidCsv())));
        assertEquals(LocalDate.of(1900, 1, 1), Lookup.newYearDate(0));
    }

    @Test
    void failedLoad_doesNotCorruptPreviouslyPublishedData() {
        // Prime with valid data first.
        assertDoesNotThrow(() -> Lookup.load(streamOf(fullValidCsv())));
        assertEquals(LocalDate.of(1900, 1, 1), Lookup.newYearDate(0));

        // A subsequent failed load must not overwrite the good data already published.
        assertThrows(CalendarDataUnavailableException.class, () -> Lookup.load(streamOf("")));
        assertEquals(LocalDate.of(1900, 1, 1), Lookup.newYearDate(0));
    }
}
