package com.littlepay.transit_calculator.domain;

import java.time.LocalDateTime;

public record Trip(LocalDateTime started,
                   LocalDateTime finished, // Can be null for incomplete trips
                   Long durationSecs,      // Can be 0 or null for incomplete/cancelled
                   String fromStopId,
                   String toStopId,        // Can be null for incomplete trips
                   Money chargeAmount,
                   String companyId,
                   String busId,
                   String pan,
                   TripStatus status) {
}
