package com.example.parking.repository;

import com.example.parking.model.BookingStatus;
import com.example.parking.model.ParkingBooking;
import com.example.parking.model.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ParkingBookingRepository extends JpaRepository<ParkingBooking, Long> {

    boolean existsByVehicleNumberAndBookingStatus(String vehicleNumber, BookingStatus bookingStatus);

    long countByBookingStatus(BookingStatus bookingStatus);

    @Query("select b from ParkingBooking b join fetch b.parkingSlot where b.id = :id")
    Optional<ParkingBooking> findWithSlotById(@Param("id") Long id);

    /** Locks the booking row so it cannot be checked out or paid twice at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from ParkingBooking b join fetch b.parkingSlot where b.id = :id")
    Optional<ParkingBooking> findByIdForUpdate(@Param("id") Long id);

    @Query("select b from ParkingBooking b join fetch b.parkingSlot "
            + "where b.bookingStatus = :status order by b.entryTime desc")
    List<ParkingBooking> findByStatusWithSlot(@Param("status") BookingStatus status);

    @Query("select b from ParkingBooking b join fetch b.parkingSlot "
            + "where b.bookingStatus = :status and b.paymentStatus = :payment order by b.exitTime desc")
    List<ParkingBooking> findByStatusAndPaymentWithSlot(@Param("status") BookingStatus status,
                                                        @Param("payment") PaymentStatus payment);

    @Query("select sum(b.parkingFee) from ParkingBooking b "
            + "where b.paymentStatus = :payment and b.paidAt >= :start and b.paidAt < :end")
    BigDecimal sumFeeByPaymentBetween(@Param("payment") PaymentStatus payment,
                                      @Param("start") Instant start,
                                      @Param("end") Instant end);

    @Query("select count(b) from ParkingBooking b "
            + "where b.paymentStatus = :payment and b.paidAt >= :start and b.paidAt < :end")
    long countByPaymentBetween(@Param("payment") PaymentStatus payment,
                               @Param("start") Instant start,
                               @Param("end") Instant end);
}
