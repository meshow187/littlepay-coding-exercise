package com.littlepay.transit_calculator.service;

import com.littlepay.transit_calculator.domain.Money;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class FareMatrix {

    // Define the exact fares between stops
    private static final Map<Set<String>, Money> FARES = Map.of(
            Set.of("Stop1", "Stop2"), Money.of("3.25"),
            Set.of("Stop2", "Stop3"), Money.of("5.50"),
            Set.of("Stop1", "Stop3"), Money.of("7.30"));

    /**
     * Calculates the exact fare between two stops.
     * Prices apply for travel in either direction.
     */
    public Money getFare(String fromStop, String toStop) {
        if (fromStop.equals(toStop)) {
            return Money.zero(); // Cancelled trip
        }
        Set<String> route = Set.of(fromStop, toStop);
        if (!FARES.containsKey(route)) {
            // FIX: Defensive coding. Do not give free rides for unknown routes.
            throw new IllegalArgumentException(
                    "System Error: No fare configured for route between " + fromStop + " and " + toStop);
        }
        return FARES.get(route);
    }

    /**
     * Determines the maximum possible charge from a given origin stop.
     */
    public Money getMaxFareFrom(String originStop) {
        return switch (originStop) {
            case "Stop1", "Stop3" -> Money.of("7.30"); // Max possible from 1 or 3 is to the other end
            case "Stop2" -> Money.of("5.50"); // Max possible from 2 is to 3
            default ->
                throw new IllegalArgumentException("System Error: Unknown origin stop for max fare: " + originStop);
        };
    }
}
