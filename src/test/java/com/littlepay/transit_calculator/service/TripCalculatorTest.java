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
                () -> assertEquals("Stop2", trip.toStopId()));
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
                () -> assertEquals(0L, trip.durationSecs()));
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
                () -> assertEquals("Stop2", trip.toStopId()));
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

    @Test
    void shouldProcessCombinedBusinessCases() {
        // Arrange
        // Scenario 1: Standard Completed Trip (PAN1: Stop1 -> Stop2)
        Tap pan1On = createTap(1L, "2018-01-22T10:00:00", TapType.ON, "Stop1", "PAN1");
        Tap pan1Off = createTap(2L, "2018-01-22T10:05:00", TapType.OFF, "Stop2", "PAN1");

        // Scenario 2: Cancelled Trip (PAN2: Stop2 -> Stop2)
        Tap pan2On = createTap(3L, "2018-01-22T10:10:00", TapType.ON, "Stop2", "PAN2");
        Tap pan2Off = createTap(4L, "2018-01-22T10:10:00", TapType.OFF, "Stop2", "PAN2");

        // Scenario 3: Incomplete Trip (PAN3: Stop3 -> Nowhere)
        Tap pan3On = createTap(5L, "2018-01-22T10:20:00", TapType.ON, "Stop3", "PAN3");

        // Scenario 4: Consecutive ON Taps (PAN4: Stop1 -> Stop2 -> Stop3)
        // The first ON tap (Stop1) should be ignored. The trip should be calculated
        // from Stop2 to Stop3.
        Tap pan4On1 = createTap(6L, "2018-01-22T10:30:00", TapType.ON, "Stop1", "PAN4");
        Tap pan4On2 = createTap(7L, "2018-01-22T10:35:00", TapType.ON, "Stop2", "PAN4");
        Tap pan4Off = createTap(8L, "2018-01-22T10:45:00", TapType.OFF, "Stop3", "PAN4");

        List<Tap> allTaps = List.of(pan1On, pan1Off, pan2On, pan2Off, pan3On, pan4On1, pan4On2, pan4Off);

        // Act
        List<Trip> trips = tripCalculator.calculateTrips(allTaps);

        // Assert
        assertEquals(4, trips.size());

        // Extract trips by PAN for granular assertions
        Trip tripPan1 = trips.stream().filter(t -> t.pan().equals("PAN1")).findFirst().orElseThrow();
        Trip tripPan2 = trips.stream().filter(t -> t.pan().equals("PAN2")).findFirst().orElseThrow();
        Trip tripPan3 = trips.stream().filter(t -> t.pan().equals("PAN3")).findFirst().orElseThrow();
        Trip tripPan4 = trips.stream().filter(t -> t.pan().equals("PAN4")).findFirst().orElseThrow();

        // Validate PAN 1: Completed
        assertAll("PAN1 Completed Trip",
                () -> assertEquals(TripStatus.COMPLETED, tripPan1.status()),
                () -> assertEquals(Money.of("3.25"), tripPan1.chargeAmount()),
                () -> assertEquals("Stop1", tripPan1.fromStopId()),
                () -> assertEquals("Stop2", tripPan1.toStopId()));

        // Validate PAN 2: Cancelled
        assertAll("PAN2 Cancelled Trip",
                () -> assertEquals(TripStatus.CANCELLED, tripPan2.status()),
                () -> assertEquals(Money.zero(), tripPan2.chargeAmount()),
                () -> assertEquals("Stop2", tripPan2.fromStopId()),
                () -> assertEquals("Stop2", tripPan2.toStopId()));

        // Validate PAN 3: Incomplete
        assertAll("PAN3 Incomplete Trip",
                () -> assertEquals(TripStatus.INCOMPLETE, tripPan3.status()),
                () -> assertEquals(Money.of("7.30"), tripPan3.chargeAmount()),
                () -> assertEquals("Stop3", tripPan3.fromStopId()),
                () -> assertNull(tripPan3.toStopId()));

        // Validate PAN 4: Consecutive ON Taps Handled Correctly
        assertAll("PAN4 Consecutive ON Taps",
                () -> assertEquals(TripStatus.COMPLETED, tripPan4.status()),
                () -> assertEquals(Money.of("5.50"), tripPan4.chargeAmount()),
                () -> assertEquals("Stop2", tripPan4.fromStopId()),
                () -> assertEquals("Stop3", tripPan4.toStopId()));
    }
}
