package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Booking;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.bookingReference = :bookingReference")
    Optional<Booking> findByBookingReference(@Param("bookingReference") String bookingReference);

    List<Booking> findByUser(User user);

    List<Booking> findByEvent(Event event);

    List<Booking> findByBookingStatus(String bookingStatus);

    List<Booking> findByUserAndBookingStatus(User user, String bookingStatus);

    List<Booking> findByEventAndBookingStatus(Event event, String bookingStatus);
}
