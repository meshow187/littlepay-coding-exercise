package com.littlepay.transit_calculator.service;

import com.littlepay.transit_calculator.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FareMatrixTest {

    @Autowired
    private FareMatrix fareMatrix;

    @Test
    void shouldCalculateCorrectFareForCompletedTrips() {
        assertEquals(Money.of("3.25"), fareMatrix.getFare("Stop1", "Stop2"));
        assertEquals(Money.of("3.25"), fareMatrix.getFare("Stop2", "Stop1")); // Reverse direction

        assertEquals(Money.of("5.50"), fareMatrix.getFare("Stop2", "Stop3"));
        assertEquals(Money.of("5.50"), fareMatrix.getFare("Stop3", "Stop2"));

        assertEquals(Money.of("7.30"), fareMatrix.getFare("Stop1", "Stop3"));
        assertEquals(Money.of("7.30"), fareMatrix.getFare("Stop3", "Stop1"));
    }

    @Test
    void shouldReturnZeroForCancelledTrips() {
        assertEquals(Money.zero(), fareMatrix.getFare("Stop1", "Stop1"));
        assertEquals(Money.zero(), fareMatrix.getFare("Stop2", "Stop2"));
    }

    @Test
    void shouldCalculateMaxFareForIncompleteTrips() {
        assertEquals(Money.of("7.30"), fareMatrix.getMaxFareFrom("Stop1"));
        assertEquals(Money.of("5.50"), fareMatrix.getMaxFareFrom("Stop2"));
        assertEquals(Money.of("7.30"), fareMatrix.getMaxFareFrom("Stop3"));
    }
}
