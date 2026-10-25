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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-25T10:00:00Z");

    @Mock private ParkingBookingRepository bookingRepository;
    @Mock private ParkingSlotRepository slotRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneId.of("Asia/Kolkata"));
        bookingService = new BookingService(bookingRepository, slotRepository, new FeeCalculationService(), fixedClock);
    }

    private ParkingSlot slot(SlotStatus status) {
        ParkingSlot slot = new ParkingSlot("A-01");
        slot.setId(1L);
        slot.setStatus(status);
        return slot;
    }

    private ParkingBooking activeBookingEnteredSecondsAgo(long secondsAgo, ParkingSlot slot) {
        ParkingBooking booking = new ParkingBooking("MH31AB1234", slot, NOW.minusSeconds(secondsAgo));
        booking.setId(7L);
        slot.setStatus(SlotStatus.OCCUPIED);
        return booking;
    }

    // ---------------- booking ----------------

    @Test
    @DisplayName("Successful booking occupies the slot and saves an ACTIVE booking")
    void successfulBooking() {
        ParkingSlot slot = slot(SlotStatus.AVAILABLE);
        when(slotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(slot));
        when(bookingRepository.existsByVehicleNumberAndBookingStatus("MH31AB1234", BookingStatus.ACTIVE)).thenReturn(false);
        when(bookingRepository.saveAndFlush(any(ParkingBooking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking(new CreateBookingRequest("mh31 ab-1234", 1L));

        assertEquals("MH31AB1234", response.vehicleNumber());
        assertEquals(BookingStatus.ACTIVE, response.bookingStatus());
        assertEquals(PaymentStatus.PENDING, response.paymentStatus());
        assertEquals(NOW, response.entryTime());
        assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
    }

    @Test
    @DisplayName("Booking an occupied slot is rejected")
    void occupiedSlotRejected() {
        when(slotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(slot(SlotStatus.OCCUPIED)));

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking(new CreateBookingRequest("MH31AB1234", 1L)));
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Booking an unknown slot ID gives not-found")
    void invalidSlotId() {
        when(slotRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(new CreateBookingRequest("MH31AB1234", 99L)));
    }

    @Test
    @DisplayName("A vehicle with an active booking cannot park again, and the slot stays free")
    void duplicateActiveBooking() {
        ParkingSlot slot = slot(SlotStatus.AVAILABLE);
        when(slotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(slot));
        when(bookingRepository.existsByVehicleNumberAndBookingStatus("MH31AB1234", BookingStatus.ACTIVE)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking(new CreateBookingRequest("MH31AB1234", 1L)));
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    // ---------------- fare ----------------

    @Test
    @DisplayName("Estimated fare uses the same fee rule as checkout")
    void estimatedFare() {
        ParkingBooking booking = activeBookingEnteredSecondsAgo(3 * 3600 + 1, slot(SlotStatus.AVAILABLE));
        when(bookingRepository.findWithSlotById(7L)).thenReturn(Optional.of(booking));

        FareResponse fare = bookingService.getEstimatedFare(7L);

        assertEquals(new BigDecimal("100.00"), fare.estimatedFee());
        assertEquals(3 * 3600 + 1, fare.durationSeconds());
    }

    // ---------------- checkout ----------------

    @Test
    @DisplayName("Successful checkout completes the booking with the correct fee and PENDING payment")
    void successfulCheckout() {
        ParkingSlot slot = slot(SlotStatus.AVAILABLE);
        ParkingBooking booking = activeBookingEnteredSecondsAgo(2 * 3600, slot);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.checkout(7L);

        assertEquals(BookingStatus.COMPLETED, response.bookingStatus());
        assertEquals(PaymentStatus.PENDING, response.paymentStatus());
        assertNull(response.paidAt());
        assertEquals(new BigDecimal("50.00"), response.parkingFee());
        assertEquals(NOW, response.exitTime());
    }

    @Test
    @DisplayName("Checkout releases the parking slot")
    void checkoutReleasesSlot() {
        ParkingSlot slot = slot(SlotStatus.AVAILABLE);
        ParkingBooking booking = activeBookingEnteredSecondsAgo(600, slot);
        assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));

        bookingService.checkout(7L);

        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        verify(slotRepository).save(slot);
    }

    @Test
    @DisplayName("Checking out an already completed booking is rejected")
    void checkoutTwiceRejected() {
        ParkingSlot slot = slot(SlotStatus.AVAILABLE);
        ParkingBooking booking = activeBookingEnteredSecondsAgo(600, slot);
        booking.setBookingStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () -> bookingService.checkout(7L));
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkout of an unknown booking gives not-found")
    void checkoutUnknownBooking() {
        when(bookingRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookingService.checkout(404L));
    }

    // ---------------- payment ----------------

    @Test
    @DisplayName("Marking a completed booking as paid records the payment time")
    void markAsPaid() {
        ParkingBooking booking = activeBookingEnteredSecondsAgo(600, slot(SlotStatus.AVAILABLE));
        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setExitTime(NOW);
        booking.setParkingFee(new BigDecimal("50.00"));
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponse response = bookingService.markAsPaid(7L);

        assertEquals(PaymentStatus.PAID, response.paymentStatus());
        assertEquals(NOW, response.paidAt());
    }

    @Test
    @DisplayName("An active booking cannot be paid, and a paid booking cannot be paid twice")
    void invalidPayments() {
        ParkingBooking active = activeBookingEnteredSecondsAgo(600, slot(SlotStatus.AVAILABLE));
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(active));
        assertThrows(ConflictException.class, () -> bookingService.markAsPaid(7L));

        active.setBookingStatus(BookingStatus.COMPLETED);
        active.setPaymentStatus(PaymentStatus.PAID);
        assertThrows(ConflictException.class, () -> bookingService.markAsPaid(7L));
    }
}
