package com.nepalidate.ADBS.NepaliDateConverter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Core AD ↔ BS converter.
 *
 * Public API is backward-compatible with the original AdBs class.
 * All conversion logic uses static helpers so callers get no per-instance overhead.
 */
public class AdBs {

    static final String DEFAULT_FORMAT = "ddMMyyyy";

    private static final DateTimeFormatter INPUT_AD_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Pattern BS_FORMAT_PATTERN = Pattern.compile("\\d{8}");

    public AdBs() {}

    /**
     * Converts a BS date string (format {@code ddMMyyyy}) to a Gregorian {@link Date}.
     */
    public Date convertBsToAd(String bsDate) {
        if (!BS_FORMAT_PATTERN.matcher(bsDate).matches()) {
            throw new InvalidDateFormatException(
                    "Invalid date: '" + bsDate + "'. Expected 8 digits in DDMMYYYY order.");
        }
        int bsDayOfMonth = Integer.parseInt(bsDate.substring(0, 2));
        int bsMonth      = Integer.parseInt(bsDate.substring(2, 4));
        int bsYear       = Integer.parseInt(bsDate.substring(4));

        validateBsDate(bsYear, bsMonth, bsDayOfMonth);
        LocalDate result = toAdLocalDate(bsYear, bsMonth, bsDayOfMonth);
        return Date.from(result.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts an AD date string (format {@code dd-MM-yyyy}) to a BS date string
     * ({@code bsYear-bsMonth-bsDay}, no zero-padding).
     */
    public String convertAdToBs(String adDate) throws ParseException {
        LocalDate inputDate = LocalDate.parse(adDate, INPUT_AD_FORMATTER);
        int[] bs = toBsComponents(inputDate);
        return bs[0] + "-" + bs[1] + "-" + bs[2];
    }

    /**
     * Validates a BS date. Throws a descriptive exception on any violation.
     * Returns {@code true} if valid (never returns {@code false}).
     */
    public boolean validateBsDate(int bsYear, int bsMonth, int bsDayOfMonth) {
        if (bsYear < Lookup.START_YEAR) {
            throw new DateRangeNotSupported(
                    "Nepali year " + bsYear + " is before the supported range. " +
                    "Supported years are " + Lookup.START_YEAR + " to " + Lookup.END_YEAR + ".");
        }
        if (bsYear > Lookup.END_YEAR) {
            throw new DateRangeNotSupported(
                    "Nepali year " + bsYear + " is beyond the supported range. " +
                    "Supported years are " + Lookup.START_YEAR + " to " + Lookup.END_YEAR + ".");
        }
        if (bsMonth < 1 || bsMonth > 12) {
            throw new InvalidBsDayOfMonthException(
                    "Month " + bsMonth + " is not valid. " +
                    "Nepali calendar months are numbered 1 to 12.");
        }
        int maxDays = Lookup.monthDays[bsYear - Lookup.START_YEAR][bsMonth - 1];
        if (bsDayOfMonth < 1 || bsDayOfMonth > maxDays) {
            throw new InvalidBsDayOfMonthException(String.format(
                    "Day %d is not valid for month %d of Nepali year %d. " +
                    "This month only has %d days.",
                    bsDayOfMonth, bsMonth, bsYear, maxDays));
        }
        return true;
    }

    // ---------------------------------------------------------------------------
    // Package-internal static helpers — used by NDC to avoid going through the
    // public Date-returning API and back. Not part of the public contract.
    // ---------------------------------------------------------------------------

    /** BS date → AD LocalDate. Caller must have already validated inputs. */
    static LocalDate toAdLocalDate(int bsYear, int bsMonth, int bsDayOfMonth) {
        int idx = bsYear - Lookup.START_YEAR;
        int dayOffset = bsDayOfMonth - 1;
        for (int m = 0; m < bsMonth - 1; m++) {
            dayOffset += Lookup.monthDays[idx][m];
        }
        return Lookup.newYearDates[idx].plusDays(dayOffset);
    }

    /**
     * AD LocalDate → BS components as {@code int[]{year, month, day}}.
     * Uses the +57 estimation to find the BS year in O(1–2) steps rather than O(132).
     */
    static int[] toBsComponents(LocalDate adDate) {
        // BS year ≈ AD year + 57; starting one below is always safe.
        int idx = adDate.getYear() + 56 - Lookup.START_YEAR;
        idx = Math.max(0, Math.min(idx, Lookup.YEAR_COUNT - 2));

        while (idx < Lookup.YEAR_COUNT - 1 && !adDate.isBefore(Lookup.newYearDates[idx + 1])) {
            idx++;
        }
        while (idx > 0 && adDate.isBefore(Lookup.newYearDates[idx])) {
            idx--;
        }

        if (adDate.isBefore(Lookup.newYearDates[0])) {
            throw new DateRangeNotSupported(
                    "English date " + adDate + " is before the supported range. " +
                    "Dates from April 13, 1913 onwards are supported.");
        }

        long dayOffset = ChronoUnit.DAYS.between(Lookup.newYearDates[idx], adDate);
        int bsMonth = 0;
        while (bsMonth < 12 && dayOffset >= Lookup.monthDays[idx][bsMonth]) {
            dayOffset -= Lookup.monthDays[idx][bsMonth];
            bsMonth++;
        }

        return new int[]{Lookup.START_YEAR + idx, bsMonth + 1, (int) dayOffset + 1};
    }
}
