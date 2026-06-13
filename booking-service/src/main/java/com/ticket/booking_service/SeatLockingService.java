package com.ticket.booking_service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class SeatLockingService {

    private final StringRedisTemplate redisTemplate;

    public SeatLockingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean lockSeat(String concertId, String seatId, String userId) {
        String lockKey = "LOCK:CONCERT:" + concertId + ":SEAT:" + seatId;
        // Atomic SETNX with a 10-minute TTL
        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, userId, Duration.ofMinutes(10));
        return Boolean.TRUE.equals(isLocked);
    }

    public void releaseLock(String concertId, String seatId) {
        String lockKey = "LOCK:CONCERT:" + concertId + ":SEAT:" + seatId;
        redisTemplate.delete(lockKey);
    }
}