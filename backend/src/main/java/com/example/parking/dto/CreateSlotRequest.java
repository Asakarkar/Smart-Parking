package com.example.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSlotRequest(
        @NotBlank(message = "Slot number is required")
        @Size(max = 20, message = "Slot number can be at most 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "Slot number can contain only letters, digits and hyphens")
        String slotNumber) { }
