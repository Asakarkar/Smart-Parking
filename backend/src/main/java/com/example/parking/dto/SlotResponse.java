package com.example.parking.dto;

import com.example.parking.model.SlotStatus;

import java.time.Instant;

/** vehicleNumber is filled only while the slot is occupied. */
public record SlotResponse(Long id, String slotNumber, SlotStatus status, String vehicleNumber,
                           Instant createdAt, Instant updatedAt) { }
