package com.example.parking.service;

import com.example.parking.dto.RevenueReportResponse;
import com.example.parking.model.PaymentStatus;
import com.example.parking.repository.ParkingBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ReportService {

    private final ParkingBookingRepository bookingRepository;
    private final Clock clock;

    public ReportService(ParkingBookingRepository bookingRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    /**
     * Revenue = sum of fees of PAID bookings whose payment was recorded on the given date.
     * The date is read in the application's timezone (Asia/Kolkata by default). Pending payments are never counted.
     */
    @Transactional(readOnly = true)
    public RevenueReportResponse getDailyRevenue(LocalDate date) {
        ZoneId zone = clock.getZone();
        LocalDate reportDate = date != null ? date : LocalDate.now(clock);

        Instant start = reportDate.atStartOfDay(zone).toInstant();
        Instant end = reportDate.plusDays(1).atStartOfDay(zone).toInstant();

        BigDecimal total = bookingRepository.sumFeeByPaymentBetween(PaymentStatus.PAID, start, end);
        long count = bookingRepository.countByPaymentBetween(PaymentStatus.PAID, start, end);

        BigDecimal safeTotal = (total == null ? BigDecimal.ZERO : total).setScale(2);
        return new RevenueReportResponse(reportDate, zone.getId(), safeTotal, count);
    }
}
