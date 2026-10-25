package com.example.parking.controller;

import com.example.parking.dto.BookingResponse;
import com.example.parking.dto.CreateBookingRequest;
import com.example.parking.dto.FareResponse;
import com.example.parking.model.PaymentStatus;
import com.example.parking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/active")
    @Operation(summary = "Get active bookings with duration and estimated fee")
    public List<BookingResponse> getActive() {
        return bookingService.getActiveBookings();
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed bookings, optionally filtered by paymentStatus (PENDING or PAID)")
    public List<BookingResponse> getCompleted(@RequestParam(required = false) PaymentStatus paymentStatus) {
        return bookingService.getCompletedBookings(paymentStatus);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a vehicle entry (create a booking)")
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/{id}/fare")
    @Operation(summary = "Calculate the current estimated fare of an active booking")
    public FareResponse getFare(@PathVariable Long id) {
        return bookingService.getEstimatedFare(id);
    }

    @PostMapping("/{id}/checkout")
    @Operation(summary = "Check a vehicle out: calculate the final fee and release the slot")
    public BookingResponse checkout(@PathVariable Long id) {
        return bookingService.checkout(id);
    }

    @PatchMapping("/{id}/payment")
    @Operation(summary = "Mark a completed booking as PAID")
    public BookingResponse markAsPaid(@PathVariable Long id) {
        return bookingService.markAsPaid(id);
    }
}
