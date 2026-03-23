package com.littlepay.transit_calculator.service;

import com.littlepay.transit_calculator.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TripCalculatorTest {

    private TripCalculator tripCalculator;

    @BeforeEach
    void setUp() {
        FareMatrix fareMatrix = new FareMatrix();
        tripCalculator = new TripCalculator(fareMatrix);
    }

    @Test
    void shouldProcessCompletedTrip() {
        // Arrange
        Tap tapOn = createTap(1L, "2018-01-22T13:00:00", TapType.ON, "Stop1", "PAN1");
        Tap tapOff = createTap(2L, "2018-01-22T13:05:00", TapType.OFF, "Stop2", "PAN1");

        // Act
        List<Trip> trips = tripCalculator.calculateTrips(List.of(tapOn, tapOff));

        // Assert
        assertEquals(1, trips.size());
        Trip trip = trips.get(0);

        assertAll(
                () -> assertEquals(TripStatus.COMPLETED, trip.status()),
                () -> assertEquals(Money.of("3.25"), trip.chargeAmount()),
                () -> assertEquals(300L, trip.durationSecs()), // 5 minutes = 300 seconds
                () -> assertEquals("Stop1", trip.fromStopId()),
                () -> assertEquals("Stop2", trip.toStopId())
        );
    }

    @Test
    void shouldProcessIncompleteTrip() {
        // Arrange (Only an ON tap)
        Tap tapOn = createTap(1L, "2018-01-22T13:00:00", TapType.ON, "Stop1", "PAN1");

        // Act
        List<Trip> trips = tripCalculator.calculateTrips(List.of(tapOn));

        // Assert
        assertEquals(1, trips.size());
        Trip trip = trips.get(0);

        assertAll(
                () -> assertEquals(TripStatus.INCOMPLETE, trip.status()),
                () -> assertEquals(Money.of("7.30"), trip.chargeAmount()), // Max fare from Stop1
                () -> assertEquals("Stop1", trip.fromStopId()),
                () -> assertNull(trip.toStopId()),
                () -> assertNull(trip.finished()),
                () -> assertEquals(0L, trip.durationSecs())
        );
    }

    @Test
    void shouldProcessCancelledTrip() {
        // Arrange (ON and OFF at the same stop)
        Tap tapOn = createTap(1L, "2018-01-22T13:00:00", TapType.ON, "Stop2", "PAN1");
        Tap tapOff = createTap(2L, "2018-01-22T13:05:00", TapType.OFF, "Stop2", "PAN1");

        // Act
        List<Trip> trips = tripCalculator.calculateTrips(List.of(tapOn, tapOff));

        // Assert
        assertEquals(1, trips.size());
        Trip trip = trips.get(0);

        assertAll(
                () -> assertEquals(TripStatus.CANCELLED, trip.status()),
                () -> assertEquals(Money.zero(), trip.chargeAmount()),
                () -> assertEquals("Stop2", trip.fromStopId()),
                () -> assertEquals("Stop2", trip.toStopId())
        );
    }

    @Test
    void shouldHandleOutofOrderTaps() {
        // Arrange (Provide OFF before ON in the list)
        Tap tapOn = createTap(1L, "2018-01-22T13:00:00", TapType.ON, "Stop1", "PAN1");
        Tap tapOff = createTap(2L, "2018-01-22T13:05:00", TapType.OFF, "Stop2", "PAN1");

        // Act
        List<Trip> trips = tripCalculator.calculateTrips(List.of(tapOff, tapOn)); // Passed in reverse order

        // Assert
        assertEquals(1, trips.size());
        assertEquals(TripStatus.COMPLETED, trips.get(0).status());
    }

    // Helper method to create clean test data
    private Tap createTap(Long id, String dateTime, TapType type, String stopId, String pan) {
        return new Tap(id, LocalDateTime.parse(dateTime), type, stopId, "Company1", "Bus37", pan);
    }
}
