package com.example.parking.repository;

import com.example.parking.model.ParkingSlot;
import com.example.parking.model.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findAllByOrderByIdAsc();

    List<ParkingSlot> findByStatusOrderByIdAsc(SlotStatus status);

    boolean existsBySlotNumberIgnoreCase(String slotNumber);

    long countByStatus(SlotStatus status);

    /** Locks the slot row until the transaction ends, so two people cannot book it at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSlot s where s.id = :id")
    Optional<ParkingSlot> findByIdForUpdate(@Param("id") Long id);
}
