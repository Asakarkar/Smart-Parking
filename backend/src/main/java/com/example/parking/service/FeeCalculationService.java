package com.example.parking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * The ONLY place where parking fees are calculated.
 * Rule: Rs 50 for every 3 hours of parking or any part of 3 hours.
 * Fee = 50 x ceiling(seconds / 10800). Integer maths is used, so there are no rounding surprises.
 */
@Service
public class FeeCalculationService {

    public static final BigDecimal RATE_PER_BLOCK = new BigDecimal("50");
    public static final long BLOCK_SECONDS = 3L * 60 * 60; // 10,800

    public BigDecimal calculateFee(Instant entryTime, Instant endTime) {
        return calculateFee(durationSeconds(entryTime, endTime));
    }

    public BigDecimal calculateFee(long durationSeconds) {
        if (durationSeconds <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        long blocks = (durationSeconds + BLOCK_SECONDS - 1) / BLOCK_SECONDS; // ceiling division
        return RATE_PER_BLOCK.multiply(BigDecimal.valueOf(blocks)).setScale(2);
    }

    /** Whole seconds between two instants; a partial second counts as a full second. Never negative. */
    public long durationSeconds(Instant entryTime, Instant endTime) {
        Duration duration = Duration.between(entryTime, endTime);
        if (duration.isNegative() || duration.isZero()) {
            return 0;
        }
        return duration.getSeconds() + (duration.getNano() > 0 ? 1 : 0);
    }
}
