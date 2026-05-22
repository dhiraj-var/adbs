package com.nepalidate.ADBS.NepaliDateConverter;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Public API for AD ↔ BS conversion. Drop-in replacement for the original NDC.
 * Same method signatures, same input/output formats, same exceptions.
 */
@Component
public class NDC {

    private static final Pattern AD_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern BS_PATTERN = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
    private static final DateTimeFormatter AD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Converts an AD date to a BS date.
     *
     * @param adDate Gregorian date in {@code yyyy-MM-dd} format (e.g. {@code "2026-04-14"})
     * @return Bikram Sambat date in {@code yyyy/MM/dd} format (e.g. {@code "2083/01/01"})
     */
    public String adToBs(String adDate) {
        if (adDate == null) {
            throw new InvalidDateFormatException(
                    "Invalid English date: null. Please use the format YYYY-MM-DD (example: 2026-04-14).");
        }
        if (!AD_PATTERN.matcher(adDate).matches()) {
            throw new InvalidDateFormatException(
                    "Invalid English date: '" + adDate + "'. " +
                    "Please use the format YYYY-MM-DD (example: 2026-04-14).");
        }
        try {
            LocalDate ad = LocalDate.parse(adDate, AD_FORMATTER);
            int[] bs = AdBs.toBsComponents(ad);
            return String.format("%04d/%02d/%02d", bs[0], bs[1], bs[2]);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException(
                    "'" + adDate + "' is not a valid date. Please check the day and month values.");
        }
    }

    /**
     * Converts a BS date to an AD date.
     *
     * @param bsDate Bikram Sambat date in {@code yyyy/MM/dd} format (e.g. {@code "2083/01/01"})
     * @return Gregorian date in {@code yyyy-MM-dd} format (e.g. {@code "2026-04-14"})
     */
    public String bsToAd(String bsDate) {
        if (bsDate == null) {
            throw new InvalidDateFormatException(
                    "Invalid Nepali date: null. Please use the format YYYY/MM/DD (example: 2083/01/15).");
        }
        if (!BS_PATTERN.matcher(bsDate).matches()) {
            throw new InvalidDateFormatException(
                    "Invalid Nepali date: '" + bsDate + "'. " +
                    "Please use the format YYYY/MM/DD (example: 2083/01/15).");
        }
        String[] parts = bsDate.split("/");
        int yearBs  = Integer.parseInt(parts[0]);
        int monthBs = Integer.parseInt(parts[1]);
        int dayBs   = Integer.parseInt(parts[2]);

        // Month is checked first: the original API threw DateRangeNotSupported here,
        // so that exception type is preserved for backward compatibility.
        if (monthBs < 1 || monthBs > 12) {
            throw new DateRangeNotSupported(
                    "Month " + monthBs + " is not valid. " +
                    "Nepali calendar months are numbered 1 to 12.");
        }
        if (yearBs < Lookup.START_YEAR || yearBs > Lookup.END_YEAR) {
            throw new DateRangeNotSupported(
                    "Nepali year " + yearBs + " is not supported. " +
                    "Supported range is " + Lookup.START_YEAR + " to " + Lookup.END_YEAR + ".");
        }
        int maxDays = Lookup.monthDays[yearBs - Lookup.START_YEAR][monthBs - 1];
        if (dayBs < 1 || dayBs > maxDays) {
            throw new InvalidBsDayOfMonthException(String.format(
                    "Day %d is not valid for month %d of Nepali year %d. " +
                    "This month only has %d days.",
                    dayBs, monthBs, yearBs, maxDays));
        }
        return AdBs.toAdLocalDate(yearBs, monthBs, dayBs).format(AD_FORMATTER);
    }

    /**
     * Validates a BS date, throwing a descriptive exception if invalid.
     *
     * @param bsDate     Bikram Sambat date in {@code yyyy/MM/dd} format
     * @param field_name Field label included in exception messages for easier debugging
     * @return {@code true} if the date is valid
     */
    public boolean validateDate_bs(String bsDate, String field_name) {
        if (field_name == null) field_name = "unknown field";
        if (bsDate == null) {
            throw new InvalidDateFormatException(
                    "Invalid Nepali date for '" + field_name + "': null. " +
                    "Please use the format YYYY/MM/DD (example: 2083/01/15).");
        }
        if (!BS_PATTERN.matcher(bsDate).matches()) {
            throw new InvalidDateFormatException(
                    "Invalid Nepali date for '" + field_name + "': '" + bsDate + "'. " +
                    "Please use the format YYYY/MM/DD (example: 2083/01/15).");
        }
        String[] parts = bsDate.split("/");
        int yearBs  = Integer.parseInt(parts[0]);
        int monthBs = Integer.parseInt(parts[1]);
        int dayBs   = Integer.parseInt(parts[2]);

        if (yearBs < Lookup.START_YEAR || yearBs > Lookup.END_YEAR) {
            throw new DateRangeNotSupported(
                    "Nepali year " + yearBs + " is not supported for '" + field_name + "'. " +
                    "Supported range is " + Lookup.START_YEAR + " to " + Lookup.END_YEAR + ".");
        }
        if (monthBs < 1 || monthBs > 12) {
            throw new DateRangeNotSupported(
                    "Month " + monthBs + " is not valid for '" + field_name + "'. " +
                    "Nepali calendar months are numbered 1 to 12.");
        }
        int dayOfMonth = Lookup.monthDays[yearBs - Lookup.START_YEAR][monthBs - 1];
        if (dayBs < 1 || dayBs > dayOfMonth) {
            throw new InvalidBsDayOfMonthException(String.format(
                    "Day %d is not valid for month %d of Nepali year %d ('%s'). " +
                    "This month only has %d days.",
                    dayBs, monthBs, yearBs, field_name, dayOfMonth));
        }
        return true;
    }
}
