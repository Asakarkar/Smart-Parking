package com.example.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateBookingRequest(
        @NotBlank(message = "Vehicle number is required")
        @Pattern(regexp = "^[A-Za-z]{2}[ -]?[0-9]{1,2}[ -]?[A-Za-z]{0,3}[ -]?[0-9]{4}$",
                message = "Vehicle number must look like MH31AB1234")
        String vehicleNumber,

        @NotNull(message = "Parking slot is required")
        Long slotId) { }
