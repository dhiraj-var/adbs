package com.nepalidate.ADBS;

import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the single-shared-instance usage pattern the README describes as
 * safe: one {@link NDC} used concurrently from many threads should never
 * produce a wrong or failed conversion.
 */
class ThreadSafetyTest {

    private static final DateTimeFormatter AD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDate RANGE_START = LocalDate.of(1913, 4, 13);
    private static final LocalDate RANGE_END = LocalDate.of(2044, 4, 12);

    @Test
    void sharedNdcInstance_survivesConcurrentUse() throws InterruptedException {
        NDC ndc = new NDC();
        int threadCount = 16;
        int iterationsPerThread = 2000;
        long rangeDays = ChronoUnit.DAYS.between(RANGE_START, RANGE_END);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger failures = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            futures.add(executor.submit(() -> {
                Random random = new Random();
                for (int i = 0; i < iterationsPerThread; i++) {
                    LocalDate ad = RANGE_START.plusDays(random.nextInt((int) rangeDays + 1));
                    String adStr = ad.format(AD_FORMATTER);
                    try {
                        String bs = ndc.adToBs(adStr);
                        String back = ndc.bsToAd(bs);
                        if (!adStr.equals(back)) {
                            failures.incrementAndGet();
                        }
                    } catch (RuntimeException e) {
                        failures.incrementAndGet();
                    }
                }
            }));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS), "Executor did not terminate in time");
        for (Future<?> f : futures) {
            assertDoesNotThrow(() -> { f.get(); }, "Worker thread threw unexpectedly");
        }
        assertEquals(0, failures.get(), "Some concurrent conversions failed or produced wrong results");
    }
}
