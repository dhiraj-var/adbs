package com.nepalidate.ADBS.NepaliDateConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Lookup {

    static final int START_YEAR = 1970;
    static final int END_YEAR = 2100;
    static final int YEAR_COUNT = END_YEAR - START_YEAR + 1; // 131

    // Populated lazily on first use via ensureLoaded(). Deliberately NOT populated
    // in a static initializer: a JVM static initializer that throws poisons the
    // class for the rest of the classloader's lifetime (ExceptionInInitializerError,
    // then NoClassDefFoundError on every subsequent access, even with a fixed CSV).
    // Using a plain lazily-invoked method means a failed load can be retried.
    private static volatile LocalDate[] newYearDatesByIndex;
    private static volatile int[][] monthDaysByIndex;

    private Lookup() {}

    private static void ensureLoaded() {
        if (newYearDatesByIndex != null) return;
        synchronized (Lookup.class) {
            if (newYearDatesByIndex != null) return;
            try (InputStream is = Lookup.class.getClassLoader().getResourceAsStream("nepali_dates.csv")) {
                if (is == null) {
                    throw new CalendarDataUnavailableException("nepali_dates.csv not found on classpath. " +
                            "Make sure the file exists in src/main/resources/.");
                }
                load(is);
            } catch (IOException e) {
                throw new CalendarDataUnavailableException(
                        "Failed to load nepali_dates.csv: " + e.getMessage());
            }
        }
    }

    /**
     * Parses calendar data from the given stream and, on success, publishes it as
     * the shared lookup data used by all conversions. On failure, throws without
     * mutating any previously-published data, so a later call with valid data
     * still succeeds. Package-private so tests can exercise malformed input
     * directly, without going through the classpath resource.
     */
    static void load(InputStream is) throws IOException {
        LocalDate[] years = new LocalDate[YEAR_COUNT];
        int[][] days = new int[YEAR_COUNT][12];

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try {
                    parseLine(line, years, days);
                } catch (RuntimeException e) {
                    throw new CalendarDataUnavailableException(
                            "Error on line " + lineNumber + " of nepali_dates.csv: " +
                            e.getMessage() + " → \"" + line + "\"");
                }
            }
        }

        validateAllYearsLoaded(years);
        validateYearLengthsMatchDateGaps(years, days);

        newYearDatesByIndex = years;
        monthDaysByIndex = days;
    }

    private static void parseLine(String line, LocalDate[] years, int[][] days) {
        String[] parts = line.split(",");
        if (parts.length != 14) {
            throw new IllegalArgumentException(
                    "expected 14 columns (bs_year, new_year_date, 12 month values) " +
                    "but found " + parts.length);
        }
        int bsYear = Integer.parseInt(parts[0].trim());
        int idx = bsYear - START_YEAR;
        if (idx < 0 || idx >= YEAR_COUNT) {
            throw new IllegalArgumentException(
                    "BS year " + bsYear + " is outside the supported range " +
                    START_YEAR + " to " + END_YEAR);
        }
        years[idx] = LocalDate.parse(parts[1].trim());
        for (int m = 0; m < 12; m++) {
            int monthDays = Integer.parseInt(parts[m + 2].trim());
            if (monthDays < 29 || monthDays > 32) {
                throw new IllegalArgumentException(
                        "month " + (m + 1) + " has " + monthDays + " days, " +
                        "which is outside the valid range of 29–32 for a Nepali month");
            }
            days[idx][m] = monthDays;
        }
    }

    private static void validateAllYearsLoaded(LocalDate[] years) {
        for (int i = 0; i < YEAR_COUNT; i++) {
            if (years[i] == null) {
                throw new CalendarDataUnavailableException(
                        "nepali_dates.csv is missing data for BS year " + (START_YEAR + i) + ". " +
                        "All years from " + START_YEAR + " to " + END_YEAR + " must be present.");
            }
        }
    }

    /**
     * A year's 12 month-day-counts must sum to exactly the number of days
     * between its new-year date and the next year's new-year date. Without
     * this check, a mismatched row surfaces far from the point of corruption
     * as a confusing "date beyond supported range" error, or worse, a
     * silently wrong conversion, instead of a clear, immediate, diagnosable
     * failure at load time.
     */
    private static void validateYearLengthsMatchDateGaps(LocalDate[] years, int[][] days) {
        for (int i = 0; i < YEAR_COUNT - 1; i++) {
            int declaredLength = 0;
            for (int m = 0; m < 12; m++) {
                declaredLength += days[i][m];
            }
            long actualGap = ChronoUnit.DAYS.between(years[i], years[i + 1]);
            if (declaredLength != actualGap) {
                throw new CalendarDataUnavailableException(String.format(
                        "nepali_dates.csv is internally inconsistent for BS year %d: " +
                        "its 12 month-day-counts sum to %d, but the gap between its new-year date (%s) " +
                        "and BS year %d's new-year date (%s) is %d days.",
                        START_YEAR + i, declaredLength, years[i], START_YEAR + i + 1, years[i + 1], actualGap));
            }
        }
    }

    /**
     * Clears cached data so the next accessor call reloads from the classpath
     * CSV. Test-only: lets a test that calls {@link #load(InputStream)} with
     * synthetic data avoid leaking that data into other tests sharing the JVM.
     */
    static void resetForTesting() {
        newYearDatesByIndex = null;
        monthDaysByIndex = null;
    }

    static LocalDate newYearDate(int bsYearIndex) {
        ensureLoaded();
        return newYearDatesByIndex[bsYearIndex];
    }

    static int monthDayCount(int bsYearIndex, int monthIndex0Based) {
        ensureLoaded();
        return monthDaysByIndex[bsYearIndex][monthIndex0Based];
    }
}
