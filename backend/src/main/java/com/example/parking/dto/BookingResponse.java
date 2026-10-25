package com.example.parking.dto;

import com.example.parking.model.BookingStatus;
import com.example.parking.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * parkingFee is the final fee once the booking is COMPLETED,
 * and the current estimate while the booking is still ACTIVE.
 */
public record BookingResponse(Long id, String vehicleNumber, Long slotId, String slotNumber,
                              Instant entryTime, Instant exitTime, long durationSeconds,
                              BigDecimal parkingFee, BookingStatus bookingStatus,
                              PaymentStatus paymentStatus, Instant paidAt, Instant createdAt) { }
