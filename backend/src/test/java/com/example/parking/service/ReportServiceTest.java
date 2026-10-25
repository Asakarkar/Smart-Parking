package com.example.parking.service;

import com.example.parking.dto.RevenueReportResponse;
import com.example.parking.model.PaymentStatus;
import com.example.parking.repository.ParkingBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    // The report day is read in India time: 25 Oct 00:00 IST = 24 Oct 18:30 UTC.
    private static final Instant DAY_START = Instant.parse("2026-10-24T18:30:00Z");
    private static final Instant DAY_END = Instant.parse("2026-10-25T18:30:00Z");

    @Mock private ParkingBookingRepository bookingRepository;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneId.of("Asia/Kolkata"));
        reportService = new ReportService(bookingRepository, clock);
    }

    @Test
    @DisplayName("Revenue for a date with paid bookings")
    void revenueWithPaidBookings() {
        when(bookingRepository.sumFeeByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END))
                .thenReturn(new BigDecimal("250.00"));
        when(bookingRepository.countByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END)).thenReturn(3L);

        RevenueReportResponse report = reportService.getDailyRevenue(LocalDate.of(2026, 10, 25));

        assertEquals(new BigDecimal("250.00"), report.totalRevenue());
        assertEquals(3L, report.paidBookings());
        assertEquals(LocalDate.of(2026, 10, 25), report.date());
        assertEquals("Asia/Kolkata", report.timezone());
    }

    @Test
    @DisplayName("Revenue for a date without paid bookings is zero")
    void revenueWithoutPaidBookings() {
        when(bookingRepository.sumFeeByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END)).thenReturn(null);
        when(bookingRepository.countByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END)).thenReturn(0L);

        RevenueReportResponse report = reportService.getDailyRevenue(LocalDate.of(2026, 10, 25));

        assertEquals(new BigDecimal("0.00"), report.totalRevenue());
        assertEquals(0L, report.paidBookings());
    }

    @Test
    @DisplayName("Pending payments are never part of collected revenue")
    void pendingPaymentsExcluded() {
        when(bookingRepository.sumFeeByPaymentBetween(eq(PaymentStatus.PAID), any(), any()))
                .thenReturn(new BigDecimal("100.00"));
        when(bookingRepository.countByPaymentBetween(eq(PaymentStatus.PAID), any(), any())).thenReturn(1L);

        reportService.getDailyRevenue(LocalDate.of(2026, 10, 25));

        verify(bookingRepository, never()).sumFeeByPaymentBetween(eq(PaymentStatus.PENDING), any(), any());
        verify(bookingRepository, never()).countByPaymentBetween(eq(PaymentStatus.PENDING), any(), any());
    }

    @Test
    @DisplayName("A missing date defaults to today in the app timezone")
    void defaultsToToday() {
        when(bookingRepository.sumFeeByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END)).thenReturn(null);
        when(bookingRepository.countByPaymentBetween(PaymentStatus.PAID, DAY_START, DAY_END)).thenReturn(0L);

        assertEquals(LocalDate.of(2026, 10, 25), reportService.getDailyRevenue(null).date());
    }
}
