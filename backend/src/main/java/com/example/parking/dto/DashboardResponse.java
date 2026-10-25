package com.example.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(long totalSlots, long availableSlots, long occupiedSlots, long activeBookings,
                                LocalDate revenueDate, BigDecimal revenue, BigDecimal occupancyPercentage) { }
