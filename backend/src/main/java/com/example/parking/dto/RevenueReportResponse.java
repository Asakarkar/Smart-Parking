package com.example.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportResponse(LocalDate date, String timezone, BigDecimal totalRevenue, long paidBookings) { }
