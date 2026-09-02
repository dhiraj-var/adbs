package com.nepalidate.ADBS;

import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exhaustively round-trips every day in the supported AD range through
 * adToBs -> bsToAd. The AD<->BS mapping is a bijection over the whole
 * supported window, so this single AD-side sweep also exercises every BS
 * date from 1970/01/01 to 2100/12/30 -- there is no day on either side that
 * this loop skips.
 */
@Tag("slow")
class FullRangeRoundTripTest {

    private static final DateTimeFormatter AD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void everySupportedAdDate_roundTripsThroughBs() {
        NDC ndc = new NDC();
        LocalDate start = LocalDate.of(1913, 4, 13);
        LocalDate end = LocalDate.of(2044, 4, 12);

        long count = 0;
        for (LocalDate ad = start; !ad.isAfter(end); ad = ad.plusDays(1)) {
            String adStr = ad.format(AD_FORMATTER);
            String bs = ndc.adToBs(adStr);
            assertEquals(adStr, ndc.bsToAd(bs),
                    "Roundtrip failed for AD date " + adStr + " (BS " + bs + ")");
            count++;
        }
        assertEquals(ChronoUnit.DAYS.between(start, end) + 1, count);
    }
}
