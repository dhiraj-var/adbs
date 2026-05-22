package com.nepalidate.ADBS;

import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NepaliDateConverterTest {

    private NDC ndc;

    @BeforeEach
    void setUp() {
        ndc = new NDC();
    }

    // --- AD → BS ---

    @Test
    void adToBs_today() {
        // April 14 + 38 days = May 22; Baisakh(31) exhausted, offset 7 into Jestha → Jestha 8
        assertEquals("2083/02/08", ndc.adToBs("2026-05-22"));
    }

    @Test
    void adToBs_nepaliNewYear2083() {
        assertEquals("2083/01/01", ndc.adToBs("2026-04-14"));
    }

    @Test
    void adToBs_nepaliNewYear2081() {
        // 2081 new year is on 2024-04-13
        assertEquals("2081/01/01", ndc.adToBs("2024-04-13"));
    }

    @Test
    void adToBs_nepaliNewYear2082() {
        // 2082 new year is on 2025-04-14
        assertEquals("2082/01/01", ndc.adToBs("2025-04-14"));
    }

    @Test
    void adToBs_dayBeforeNewYear2083() {
        // One day before 2083 new year (2026-04-14); Chaitra 2082 has 30 days
        assertEquals("2082/12/30", ndc.adToBs("2026-04-13"));
    }

    @Test
    void adToBs_earlyYear() {
        // BS 1970 starts on 1913-04-13
        assertEquals("1970/01/01", ndc.adToBs("1913-04-13"));
    }

    @Test
    void adToBs_invalidFormat() {
        assertThrows(InvalidDateFormatException.class, () -> ndc.adToBs("22-05-2026"));
        assertThrows(InvalidDateFormatException.class, () -> ndc.adToBs("2026/05/22"));
    }

    // --- BS → AD ---

    @Test
    void bsToAd_today() {
        // Jestha 9 = offset 31+8 = 39 days from April 14 → May 23
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
    void bsToAd_earlyYear() {
        assertEquals("1913-04-13", ndc.bsToAd("1970/01/01"));
    }

    @Test
    void bsToAd_invalidFormat() {
        assertThrows(InvalidDateFormatException.class, () -> ndc.bsToAd("2083-01-01"));
    }

    @Test
    void bsToAd_invalidMonth() {
        assertThrows(DateRangeNotSupported.class, () -> ndc.bsToAd("2083/13/01"));
    }

    @Test
    void bsToAd_invalidDay() {
        assertThrows(InvalidBsDayOfMonthException.class, () -> ndc.bsToAd("2083/01/32"));
    }

    // --- Roundtrip ---

    @Test
    void roundtrip_adToBsToAd() {
        String[] adDates = {"2026-05-22", "2024-04-13", "2025-04-14", "2023-04-14", "1913-04-13"};
        for (String ad : adDates) {
            String bs = ndc.adToBs(ad);
            assertEquals(ad, ndc.bsToAd(bs), "Roundtrip failed for " + ad);
        }
    }

    @Test
    void roundtrip_bsToAdToBs() {
        String[] bsDates = {"2083/02/08", "2081/01/01", "2082/12/30", "1970/01/01"};
        for (String bs : bsDates) {
            String ad = ndc.bsToAd(bs);
            assertEquals(bs, ndc.adToBs(ad), "Roundtrip failed for " + bs);
        }
    }

    // --- Validation ---

    @Test
    void validateDate_bs_valid() {
        assertTrue(ndc.validateDate_bs("2083/02/09", "testField"));
    }

    @Test
    void validateDate_bs_invalidDay() {
        assertThrows(InvalidBsDayOfMonthException.class,
                () -> ndc.validateDate_bs("2083/01/32", "testField"));
    }

    @Test
    void validateDate_bs_invalidFormat() {
        assertThrows(InvalidDateFormatException.class,
                () -> ndc.validateDate_bs("2083-01-01", "testField"));
    }
}
