package com.example.parking.service;

import com.example.parking.dto.DashboardResponse;
import com.example.parking.dto.RevenueReportResponse;
import com.example.parking.model.BookingStatus;
import com.example.parking.model.SlotStatus;
import com.example.parking.repository.ParkingBookingRepository;
import com.example.parking.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingBookingRepository bookingRepository;
    private final ReportService reportService;

    public DashboardService(ParkingSlotRepository slotRepository,
                            ParkingBookingRepository bookingRepository,
                            ReportService reportService) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.reportService = reportService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getSummary(LocalDate date) {
        long total = slotRepository.count();
        long available = slotRepository.countByStatus(SlotStatus.AVAILABLE);
        long occupied = slotRepository.countByStatus(SlotStatus.OCCUPIED);
        long active = bookingRepository.countByBookingStatus(BookingStatus.ACTIVE);
        RevenueReportResponse revenue = reportService.getDailyRevenue(date);

        BigDecimal occupancy = total == 0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(occupied * 100).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        return new DashboardResponse(total, available, occupied, active,
                revenue.date(), revenue.totalRevenue(), occupancy);
    }
}
