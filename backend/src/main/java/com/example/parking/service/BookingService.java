package com.example.parking.service;

import com.example.parking.dto.BookingResponse;
import com.example.parking.dto.CreateBookingRequest;
import com.example.parking.dto.FareResponse;
import com.example.parking.exception.ConflictException;
import com.example.parking.exception.ResourceNotFoundException;
import com.example.parking.model.BookingStatus;
import com.example.parking.model.ParkingBooking;
import com.example.parking.model.ParkingSlot;
import com.example.parking.model.PaymentStatus;
import com.example.parking.model.SlotStatus;
import com.example.parking.repository.ParkingBookingRepository;
import com.example.parking.repository.ParkingSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class BookingService {

    private final ParkingBookingRepository bookingRepository;
    private final ParkingSlotRepository slotRepository;
    private final FeeCalculationService feeCalculationService;
    private final Clock clock;

    public BookingService(ParkingBookingRepository bookingRepository,
                          ParkingSlotRepository slotRepository,
                          FeeCalculationService feeCalculationService,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.feeCalculationService = feeCalculationService;
        this.clock = clock;
    }

    /** Vehicle entry: reserve the slot and save the booking in one transaction. */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        String vehicleNumber = normalizeVehicleNumber(request.vehicleNumber());

        // Lock the slot row so two requests cannot grab the same slot together.
        ParkingSlot slot = slotRepository.findByIdForUpdate(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot " + request.slotId() + " was not found."));

        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new ConflictException("Slot " + slot.getSlotNumber() + " is already occupied.");
        }
        if (bookingRepository.existsByVehicleNumberAndBookingStatus(vehicleNumber, BookingStatus.ACTIVE)) {
            throw new ConflictException("Vehicle " + vehicleNumber + " already has an active booking.");
        }

        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        ParkingBooking booking = new ParkingBooking(vehicleNumber, slot, clock.instant());
        try {
            booking = bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            // The database's unique indexes are the last line of defence against duplicates.
            throw new ConflictException("Slot or vehicle already has an active booking.");
        }
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookings() {
        return bookingRepository.findByStatusWithSlot(BookingStatus.ACTIVE).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Completed bookings, optionally only those with the given payment status. */
    @Transactional(readOnly = true)
    public List<BookingResponse> getCompletedBookings(PaymentStatus paymentStatus) {
        List<ParkingBooking> bookings = paymentStatus == null
                ? bookingRepository.findByStatusWithSlot(BookingStatus.COMPLETED)
                : bookingRepository.findByStatusAndPaymentWithSlot(BookingStatus.COMPLETED, paymentStatus);
        return bookings.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long id) {
        return toResponse(findBooking(id));
    }

    /** Current fare of an ACTIVE booking. Uses the same FeeCalculationService as checkout. */
    @Transactional(readOnly = true)
    public FareResponse getEstimatedFare(Long id) {
        ParkingBooking booking = findBooking(id);
        if (booking.getBookingStatus() != BookingStatus.ACTIVE) {
            throw new ConflictException("Booking " + id + " is already completed. Its final fee is "
                    + booking.getParkingFee() + ".");
        }
        Instant now = clock.instant();
        long seconds = feeCalculationService.durationSeconds(booking.getEntryTime(), now);
        return new FareResponse(booking.getId(), booking.getVehicleNumber(), booking.getEntryTime(), now,
                seconds, feeCalculationService.calculateFee(seconds), "INR");
    }

    /** Checkout: fix the fee, close the booking and release the slot - all or nothing. */
    @Transactional
    public BookingResponse checkout(Long id) {
        ParkingBooking booking = bookingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " was not found."));

        if (booking.getBookingStatus() != BookingStatus.ACTIVE) {
            throw new ConflictException("Booking " + id + " has already been checked out.");
        }

        Instant exitTime = clock.instant();
        booking.setExitTime(exitTime);
        booking.setParkingFee(feeCalculationService.calculateFee(booking.getEntryTime(), exitTime));
        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        ParkingSlot slot = booking.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
        bookingRepository.save(booking);
        return toResponse(booking);
    }

    /** Records payment for a completed booking. Only PAID bookings count as revenue. */
    @Transactional
    public BookingResponse markAsPaid(Long id) {
        ParkingBooking booking = bookingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " was not found."));

        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new ConflictException("Booking " + id + " is still active. Check the vehicle out before payment.");
        }
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ConflictException("Booking " + id + " is already paid.");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaidAt(clock.instant());
        return toResponse(bookingRepository.save(booking));
    }

    private ParkingBooking findBooking(Long id) {
        return bookingRepository.findWithSlotById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking " + id + " was not found."));
    }

    /** "mh 31-ab 1234" -> "MH31AB1234" so the same vehicle is always stored the same way. */
    private String normalizeVehicleNumber(String raw) {
        return raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private BookingResponse toResponse(ParkingBooking booking) {
        boolean completed = booking.getBookingStatus() == BookingStatus.COMPLETED;
        Instant end = completed ? booking.getExitTime() : clock.instant();
        long seconds = feeCalculationService.durationSeconds(booking.getEntryTime(), end);
        BigDecimal fee = completed ? booking.getParkingFee() : feeCalculationService.calculateFee(seconds);

        return new BookingResponse(booking.getId(), booking.getVehicleNumber(),
                booking.getParkingSlot().getId(), booking.getParkingSlot().getSlotNumber(),
                booking.getEntryTime(), booking.getExitTime(), seconds, fee,
                booking.getBookingStatus(), booking.getPaymentStatus(), booking.getPaidAt(), booking.getCreatedAt());
    }
}
