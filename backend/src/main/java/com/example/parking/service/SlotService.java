package com.example.parking.service;

import com.example.parking.dto.CreateSlotRequest;
import com.example.parking.dto.SlotResponse;
import com.example.parking.exception.ConflictException;
import com.example.parking.exception.ResourceNotFoundException;
import com.example.parking.model.BookingStatus;
import com.example.parking.model.ParkingBooking;
import com.example.parking.model.ParkingSlot;
import com.example.parking.model.SlotStatus;
import com.example.parking.repository.ParkingBookingRepository;
import com.example.parking.repository.ParkingSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SlotService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingBookingRepository bookingRepository;

    public SlotService(ParkingSlotRepository slotRepository, ParkingBookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots() {
        Map<Long, String> vehicles = activeVehicleBySlot();
        return slotRepository.findAllByOrderByIdAsc().stream()
                .map(slot -> toResponse(slot, vehicles.get(slot.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getAvailableSlots() {
        return slotRepository.findByStatusOrderByIdAsc(SlotStatus.AVAILABLE).stream()
                .map(slot -> toResponse(slot, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public SlotResponse getSlot(Long id) {
        ParkingSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking slot " + id + " was not found."));
        String vehicle = slot.getStatus() == SlotStatus.OCCUPIED ? activeVehicleBySlot().get(id) : null;
        return toResponse(slot, vehicle);
    }

    @Transactional
    public SlotResponse createSlot(CreateSlotRequest request) {
        String slotNumber = request.slotNumber().trim().toUpperCase(Locale.ROOT);
        if (slotRepository.existsBySlotNumberIgnoreCase(slotNumber)) {
            throw new ConflictException("Slot " + slotNumber + " already exists.");
        }
        try {
            return toResponse(slotRepository.saveAndFlush(new ParkingSlot(slotNumber)), null);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Slot " + slotNumber + " already exists.");
        }
    }

    private Map<Long, String> activeVehicleBySlot() {
        Map<Long, String> result = new HashMap<>();
        for (ParkingBooking booking : bookingRepository.findByStatusWithSlot(BookingStatus.ACTIVE)) {
            result.put(booking.getParkingSlot().getId(), booking.getVehicleNumber());
        }
        return result;
    }

    private SlotResponse toResponse(ParkingSlot slot, String vehicleNumber) {
        return new SlotResponse(slot.getId(), slot.getSlotNumber(), slot.getStatus(), vehicleNumber,
                slot.getCreatedAt(), slot.getUpdatedAt());
    }
}
