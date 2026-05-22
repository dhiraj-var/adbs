package com.nepalidate.ADBS.NepaliDateConverter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                throw new IllegalStateException("nepali_dates.csv not found on classpath");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split(",");
                    int bsYear = Integer.parseInt(parts[0].trim());
                    int idx = bsYear - START_YEAR;
                    newYearDates[idx] = LocalDate.parse(parts[1].trim());
                    for (int m = 0; m < 12; m++) {
                        monthDays[idx][m] = Integer.parseInt(parts[m + 2].trim());
                    }
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load nepali_dates.csv: " + e.getMessage());
        }
    }
}
