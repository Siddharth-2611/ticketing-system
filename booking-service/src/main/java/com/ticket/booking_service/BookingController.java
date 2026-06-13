package com.ticket.booking_service;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final SeatLockingService seatLockingService;
    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingController(SeatLockingService seatLockingService,
                             BookingRepository bookingRepository,
                             KafkaTemplate<String, String> kafkaTemplate) {
        this.seatLockingService = seatLockingService;
        this.bookingRepository = bookingRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/prices")
    public ResponseEntity<Map<String, Integer>> getCurrentPrices(@RequestParam String concertId) {
        Map<String, Integer> currentPrices = new HashMap<>();
        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            long bookedInRow = bookingRepository.countByConcertIdAndSeatIdStartingWith(concertId, row + "-");
            currentPrices.put(row, calculateSurgePrice(row, bookedInRow));
        }
        return ResponseEntity.ok(currentPrices);
    }

    @GetMapping("/locked-seats")
    public ResponseEntity<List<String>> getLockedSeats(@RequestParam String concertId) {
        return ResponseEntity.ok(bookingRepository.findBookedSeatIdsByConcertId(concertId));
    }

    @PostMapping("/lock")
    @Transactional
    public ResponseEntity<?> lockMultipleSeats(@RequestBody Map<String, Object> request) {
        String concertId = (String) request.get("concertId");
        String userId = (String) request.get("userId");
        List<String> requestedSeats = (List<String>) request.get("seats");

        if (requestedSeats == null || requestedSeats.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: No seats selected.");
        }

        List<String> successfullyLockedKeys = new ArrayList<>();
        List<Booking> savedBookings = new ArrayList<>();

        try {
            for (String seatId : requestedSeats) {
                boolean acquired = seatLockingService.lockSeat(concertId, seatId, userId);
                if (!acquired) {
                    for (String lockedSeat : successfullyLockedKeys) {
                        seatLockingService.releaseLock(concertId, lockedSeat);
                    }
                    return ResponseEntity.status(409).body("Conflict: Seat " + seatId + " is unavailable!");
                }
                successfullyLockedKeys.add(seatId);
            }

            for (String seatId : requestedSeats) {
                Booking booking = new Booking();
                booking.setConcertId(concertId);
                booking.setSeatId(seatId);
                booking.setUserId(userId);
                booking.setStatus(Booking.BookingStatus.PENDING);
                savedBookings.add(bookingRepository.save(booking));
            }
            return ResponseEntity.ok(savedBookings);
        } catch (Exception ex) {
            for (String lockedSeat : successfullyLockedKeys) {
                seatLockingService.releaseLock(concertId, lockedSeat);
            }
            return ResponseEntity.status(500).body("Internal System Fault: " + ex.getMessage());
        }
    }

    /**
     * Endpoint 4: Aggregates multiple seats into one message to prevent spam emails!
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String concertId = (String) request.get("concertId");
        List<String> seats = (List<String>) request.get("seats");

        if (seats != null && !seats.isEmpty()) {
            // Join seats using semicolons, e.g., "A-1;A-2;A-3"
            String aggregatedSeats = String.join(";", seats);

            // Sends exactly ONE Kafka payload contract: email, aggregatedSeats, showID
            String kafkaPayload = String.format("%s,%s,%s", email, aggregatedSeats, concertId);
            kafkaTemplate.send("booking-initiated-topic", kafkaPayload);
        }
        return ResponseEntity.ok(Map.of("status", "Success"));
    }

    private int calculateSurgePrice(String row, long bookedCount) {
        int basePrice = switch (row) {
            case "A" -> 2000;
            case "B" -> 1800;
            case "C" -> 1600;
            case "D" -> 1400;
            case "E" -> 1200;
            default -> 1000;
        };
        int T = 20;
        long R = T - bookedCount;
        if (R < 0) R = 0;
        double alpha = 0.5;
        double factor = 1.0 + alpha * (1.0 - ((double) R / (double) T));
        return (int) (basePrice * factor);
    }
}