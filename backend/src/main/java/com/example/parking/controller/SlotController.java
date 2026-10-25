package com.example.parking.controller;

import com.example.parking.dto.CreateSlotRequest;
import com.example.parking.dto.SlotResponse;
import com.example.parking.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@Tag(name = "Parking Slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    @Operation(summary = "Get all parking slots (with the vehicle number when occupied)")
    public List<SlotResponse> getAll() {
        return slotService.getAllSlots();
    }

    @GetMapping("/available")
    @Operation(summary = "Get only the available parking slots")
    public List<SlotResponse> getAvailable() {
        return slotService.getAvailableSlots();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a parking slot by ID")
    public SlotResponse getById(@PathVariable Long id) {
        return slotService.getSlot(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new parking slot")
    public SlotResponse create(@Valid @RequestBody CreateSlotRequest request) {
        return slotService.createSlot(request);
    }
}
