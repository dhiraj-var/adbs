package com.nepalidate.ADBS.NepaliDateConverter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public final class Lookup {

    static final int START_YEAR = 1970;
    static final int END_YEAR = 2100;
    static final int YEAR_COUNT = END_YEAR - START_YEAR + 1; // 131

    // Pre-parsed AD date for Baisakh 1 of each BS year. Index = bsYear - START_YEAR.
    static final LocalDate[] newYearDates = new LocalDate[YEAR_COUNT];

    // Days in each of the 12 months for each BS year. Index = bsYear - START_YEAR.
    static final int[][] monthDays = new int[YEAR_COUNT][12];

    static {
        loadFromCsv();
    }

    private Lookup() {}

    private static void loadFromCsv() {
        try (InputStream is = Lookup.class.getClassLoader().getResourceAsStream("nepali_dates.csv")) {
            if (is == null) {
                throw new IllegalStateException("nepali_dates.csv not found on classpath. " +
                        "Make sure the file exists in src/main/resources/.");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    try {
                        parseLine(line, lineNumber);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Error on line " + lineNumber + " of nepali_dates.csv: " +
                                e.getMessage() + " → \"" + line + "\"");
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(
                    "Failed to load nepali_dates.csv: " + e.getMessage());
        }

        validateAllYearsLoaded();
    }

    private static void parseLine(String line, int lineNumber) {
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
        newYearDates[idx] = LocalDate.parse(parts[1].trim());
        for (int m = 0; m < 12; m++) {
            int days = Integer.parseInt(parts[m + 2].trim());
            if (days < 29 || days > 32) {
                throw new IllegalArgumentException(
                        "month " + (m + 1) + " has " + days + " days, " +
                        "which is outside the valid range of 29–32 for a Nepali month");
            }
            monthDays[idx][m] = days;
        }
    }

    private static void validateAllYearsLoaded() {
        for (int i = 0; i < YEAR_COUNT; i++) {
            if (newYearDates[i] == null) {
                throw new ExceptionInInitializerError(
                        "nepali_dates.csv is missing data for BS year " + (START_YEAR + i) + ". " +
                        "All years from " + START_YEAR + " to " + END_YEAR + " must be present.");
            }
        }
    }
}
