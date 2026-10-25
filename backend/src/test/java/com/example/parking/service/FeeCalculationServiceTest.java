package com.example.parking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeeCalculationServiceTest {

    private final FeeCalculationService service = new FeeCalculationService();
    private final Instant entry = Instant.parse("2026-10-25T10:00:00Z");

    private BigDecimal feeFor(Duration duration) {
        return service.calculateFee(entry, entry.plus(duration));
    }

    @Test
    @DisplayName("Zero duration costs Rs 0")
    void zeroDuration() {
        assertEquals(new BigDecimal("0.00"), feeFor(Duration.ZERO));
    }

    @Test
    @DisplayName("30 minutes costs Rs 50")
    void thirtyMinutes() {
        assertEquals(new BigDecimal("50.00"), feeFor(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("2 hours 15 minutes costs Rs 50")
    void twoHoursFifteenMinutes() {
        assertEquals(new BigDecimal("50.00"), feeFor(Duration.ofHours(2).plusMinutes(15)));
    }

    @Test
    @DisplayName("Exactly 3 hours costs Rs 50")
    void exactlyThreeHours() {
        assertEquals(new BigDecimal("50.00"), feeFor(Duration.ofHours(3)));
    }

    @Test
    @DisplayName("3 hours 1 second costs Rs 100")
    void threeHoursOneSecond() {
        assertEquals(new BigDecimal("100.00"), feeFor(Duration.ofHours(3).plusSeconds(1)));
    }

    @Test
    @DisplayName("Exactly 6 hours costs Rs 100")
    void exactlySixHours() {
        assertEquals(new BigDecimal("100.00"), feeFor(Duration.ofHours(6)));
    }

    @Test
    @DisplayName("6 hours 10 minutes costs Rs 150")
    void sixHoursTenMinutes() {
        assertEquals(new BigDecimal("150.00"), feeFor(Duration.ofHours(6).plusMinutes(10)));
    }

    @Test
    @DisplayName("Half a second still counts as a positive duration (Rs 50)")
    void subSecondDuration() {
        assertEquals(new BigDecimal("50.00"), feeFor(Duration.ofMillis(500)));
    }

    @Test
    @DisplayName("Exit before entry is treated as zero duration")
    void negativeDuration() {
        assertEquals(new BigDecimal("0.00"), service.calculateFee(entry, entry.minusSeconds(60)));
    }

    @ParameterizedTest(name = "{0} seconds -> Rs {1}")
    @CsvSource({
            "0,0.00", "1,50.00", "10799,50.00", "10800,50.00", "10801,100.00",
            "21600,100.00", "21601,150.00", "21960,150.00", "32400,150.00", "32401,200.00"
    })
    void secondsToFee(long seconds, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), service.calculateFee(seconds));
    }
}
