package com.example.parking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FareResponse(Long bookingId, String vehicleNumber, Instant entryTime, Instant calculatedAt,
                           long durationSeconds, BigDecimal estimatedFee, String currency) { }
