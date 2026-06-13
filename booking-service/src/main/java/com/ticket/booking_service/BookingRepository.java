package com.ticket.booking_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByConcertIdAndSeatIdStartingWith(String concertId, String seatPrefix);

    // === ADD THIS NEW QUERY TO FETCH ALL RESERVED SEAT STRINGS ===
    @Query("SELECT b.seatId FROM Booking b WHERE b.concertId = :concertId")
    List<String> findBookedSeatIdsByConcertId(@Param("concertId") String concertId);
}