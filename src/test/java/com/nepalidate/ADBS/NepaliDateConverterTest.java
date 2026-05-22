package com.nepalidate.ADBS;

import com.nepalidate.ADBS.NepaliDateConverter.AdBs;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NepaliDateConverterTest {

    private NDC ndc;
    private AdBs adBs;

    @BeforeEach
    void setUp() {
        ndc  = new NDC();
        adBs = new AdBs();
    }

    // =========================================================================
    // NDC.adToBs
    // =========================================================================

    @Test
    void adToBs_typicalDate() {
        assertEquals("2083/02/08", ndc.adToBs("2026-05-22"));
    }

    @Test
    void adToBs_nepaliNewYear2083() {
        assertEquals("2083/01/01", ndc.adToBs("2026-04-14"));
    }

    @Test
    void adToBs_nepaliNewYear2082() {
        assertEquals("2082/01/01", ndc.adToBs("2025-04-14"));
    }

    @Test
    void adToBs_nepaliNewYear2081() {
        assertEquals("2081/01/01", ndc.adToBs("2024-04-13"));
    }

    @Test
    void adToBs_dayBeforeNewYear2083() {
        // Chaitra 2082 has 30 days — so the last day is the 30th
        assertEquals("2082/12/30", ndc.adToBs("2026-04-13"));
    }

    @Test
    void adToBs_firstSupportedDate() {
        assertEquals("1970/01/01", ndc.adToBs("1913-04-13"));
    }

    @Test
    void adToBs_lastSupportedDate() {
        // Last day of BS 2100 (Chaitra 30) falls on 2044-04-12
        assertEquals("2100/12/30", ndc.adToBs("2044-04-12"));
    }

    @Test
    void adToBs_nullInput() {
        InvalidDateFormatException ex = assertThrows(InvalidDateFormatException.class,
                () -> ndc.adToBs(null));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void adToBs_wrongFormat_slashes() {
        InvalidDateFormatException ex = assertThrows(InvalidDateFormatException.class,
                () -> ndc.adToBs("2026/05/22"));
        assertTrue(ex.getMessage().contains("YYYY-MM-DD"));
    }

    @Test
    void adToBs_wrongFormat_dayFirst() {
        assertThrows(InvalidDateFormatException.class, () -> ndc.adToBs("22-05-2026"));
    }

    @Test
    void adToBs_invalidDayValue() {
        // Passes regex but fails parse (month 13)
        assertThrows(InvalidDateFormatException.class, () -> ndc.adToBs("2026-13-01"));
    }

    @Test
    void adToBs_beforeSupportedRange() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.adToBs("1913-04-12"));
        assertTrue(ex.getMessage().contains("April 13, 1913"));
    }

    @Test
    void adToBs_beyondSupportedRange() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.adToBs("2044-04-13"));
        assertTrue(ex.getMessage().contains("supported range"));
    }

    // =========================================================================
    // NDC.bsToAd
    // =========================================================================

    @Test
    void bsToAd_typicalDate() {
        assertEquals("2026-05-23", ndc.bsToAd("2083/02/09"));
    }

    @Test
    void bsToAd_newYear2083() {
        assertEquals("2026-04-14", ndc.bsToAd("2083/01/01"));
    }

    @Test
    void bsToAd_newYear2082() {
        assertEquals("2025-04-14", ndc.bsToAd("2082/01/01"));
    }

    @Test
    void bsToAd_newYear2081() {
        assertEquals("2024-04-13", ndc.bsToAd("2081/01/01"));
    }

    @Test
    void bsToAd_firstSupportedDate() {
        assertEquals("1913-04-13", ndc.bsToAd("1970/01/01"));
    }

    @Test
    void bsToAd_lastSupportedDate() {
        assertEquals("2044-04-12", ndc.bsToAd("2100/12/30"));
    }

    @Test
    void bsToAd_nullInput() {
        InvalidDateFormatException ex = assertThrows(InvalidDateFormatException.class,
                () -> ndc.bsToAd(null));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void bsToAd_wrongFormat_dashes() {
        InvalidDateFormatException ex = assertThrows(InvalidDateFormatException.class,
                () -> ndc.bsToAd("2083-01-01"));
        assertTrue(ex.getMessage().contains("YYYY/MM/DD"));
    }

    @Test
    void bsToAd_invalidMonth() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.bsToAd("2083/13/01"));
        assertTrue(ex.getMessage().contains("1 to 12"));
    }

    @Test
    void bsToAd_invalidDay() {
        InvalidBsDayOfMonthException ex = assertThrows(InvalidBsDayOfMonthException.class,
                () -> ndc.bsToAd("2083/01/32"));
        assertTrue(ex.getMessage().contains("only has"));
    }

    @Test
    void bsToAd_yearBeyondRange() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.bsToAd("2101/01/01"));
        assertTrue(ex.getMessage().contains("1970 to 2100"));
    }

    @Test
    void bsToAd_yearBeforeRange() {
        assertThrows(DateRangeNotSupported.class, () -> ndc.bsToAd("1969/01/01"));
    }

    // =========================================================================
    // NDC.validateDate_bs
    // =========================================================================

    @Test
    void validateDate_bs_valid() {
        assertTrue(ndc.validateDate_bs("2083/02/08", "dateOfBirth"));
    }

    @Test
    void validateDate_bs_nullInput() {
        assertThrows(InvalidDateFormatException.class,
                () -> ndc.validateDate_bs(null, "dob"));
    }

    @Test
    void validateDate_bs_nullFieldName() {
        // null field_name should not produce NullPointerException
        assertThrows(InvalidDateFormatException.class,
                () -> ndc.validateDate_bs(null, null));
    }

    @Test
    void validateDate_bs_invalidFormat() {
        InvalidDateFormatException ex = assertThrows(InvalidDateFormatException.class,
                () -> ndc.validateDate_bs("2083-01-01", "dob"));
        assertTrue(ex.getMessage().contains("dob"));
    }

    @Test
    void validateDate_bs_invalidYear() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.validateDate_bs("2101/01/01", "startDate"));
        assertTrue(ex.getMessage().contains("startDate"));
        assertTrue(ex.getMessage().contains("1970 to 2100"));
    }

    @Test
    void validateDate_bs_invalidMonth() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> ndc.validateDate_bs("2083/13/01", "endDate"));
        assertTrue(ex.getMessage().contains("endDate"));
        assertTrue(ex.getMessage().contains("1 to 12"));
    }

    @Test
    void validateDate_bs_invalidDay() {
        InvalidBsDayOfMonthException ex = assertThrows(InvalidBsDayOfMonthException.class,
                () -> ndc.validateDate_bs("2083/01/32", "testField"));
        assertTrue(ex.getMessage().contains("testField"));
        assertTrue(ex.getMessage().contains("only has"));
    }

    // =========================================================================
    // Roundtrip
    // =========================================================================

    @Test
    void roundtrip_adToBsToAd() {
        String[] adDates = {
            "2026-05-22", "2026-04-14", "2026-04-13",
            "2024-04-13", "2025-04-14", "2023-04-14",
            "1913-04-13", "2044-04-12"
        };
        for (String ad : adDates) {
            String bs = ndc.adToBs(ad);
            assertEquals(ad, ndc.bsToAd(bs), "Roundtrip failed for AD date " + ad);
        }
    }

    @Test
    void roundtrip_bsToAdToBs() {
        String[] bsDates = {
            "2083/02/08", "2083/01/01", "2082/12/30",
            "2081/01/01", "1970/01/01", "2100/12/30"
        };
        for (String bs : bsDates) {
            String ad = ndc.bsToAd(bs);
            assertEquals(bs, ndc.adToBs(ad), "Roundtrip failed for BS date " + bs);
        }
    }

    // =========================================================================
    // AdBs direct API (public class — callers may use it without NDC)
    // =========================================================================

    @Test
    void adBs_convertAdToBs_returnsUnpaddedString() throws Exception {
        // AdBs.convertAdToBs expects "dd-MM-yyyy" format
        String result = adBs.convertAdToBs("14-04-2026");
        assertEquals("2083-1-1", result);
    }

    @Test
    void adBs_convertBsToAd_returnsDate() {
        // AdBs.convertBsToAd expects "ddMMyyyy" format
        java.util.Date result = adBs.convertBsToAd("01012083");
        assertNotNull(result);
        // Convert back to verify
        java.time.LocalDate ld = result.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        assertEquals(2026, ld.getYear());
        assertEquals(4, ld.getMonthValue());
        assertEquals(14, ld.getDayOfMonth());
    }

    @Test
    void adBs_convertBsToAd_wrongFormat() {
        assertThrows(InvalidDateFormatException.class,
                () -> adBs.convertBsToAd("01-01-2083"));
    }

    @Test
    void adBs_validateBsDate_valid() {
        assertTrue(adBs.validateBsDate(2083, 1, 1));
    }

    @Test
    void adBs_validateBsDate_yearTooLow() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> adBs.validateBsDate(1969, 1, 1));
        assertTrue(ex.getMessage().contains("1970 to 2100"));
    }

    @Test
    void adBs_validateBsDate_yearTooHigh() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> adBs.validateBsDate(2101, 1, 1));
        assertTrue(ex.getMessage().contains("1970 to 2100"));
    }

    @Test
    void adBs_validateBsDate_monthInvalid() {
        DateRangeNotSupported ex = assertThrows(DateRangeNotSupported.class,
                () -> adBs.validateBsDate(2083, 13, 1));
        assertTrue(ex.getMessage().contains("1 to 12"));
    }

    @Test
    void adBs_convertBsToAd_nullInput() {
        assertThrows(InvalidDateFormatException.class,
                () -> adBs.convertBsToAd(null));
    }

    @Test
    void adBs_convertAdToBs_nullInput() {
        assertThrows(InvalidDateFormatException.class,
                () -> adBs.convertAdToBs(null));
    }

    @Test
    void adBs_convertAdToBs_wrongFormat() {
        assertThrows(InvalidDateFormatException.class,
                () -> adBs.convertAdToBs("2026-04-14"));
    }

    @Test
    void adBs_validateBsDate_dayTooHigh() {
        InvalidBsDayOfMonthException ex = assertThrows(InvalidBsDayOfMonthException.class,
                () -> adBs.validateBsDate(2083, 1, 32));
        assertTrue(ex.getMessage().contains("only has"));
    }
}
