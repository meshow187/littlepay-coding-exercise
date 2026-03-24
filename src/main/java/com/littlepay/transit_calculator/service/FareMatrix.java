package com.littlepay.transit_calculator.service;

import com.littlepay.transit_calculator.config.TransitProperties;
import com.littlepay.transit_calculator.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class FareMatrix {

    private final TransitProperties properties;

    public FareMatrix(TransitProperties properties) {
        this.properties = properties;
    }

    /**
     * Calculates the exact fare between two stops.
     * Prices apply for travel in either direction.
     */
    public Money getFare(String fromStop, String toStop) {
        if (fromStop.equals(toStop)) {
            return Money.zero();
        }

        // Check both directions (e.g., "Stop1-Stop2" or "Stop2-Stop1")
        String route1 = fromStop + "-" + toStop;
        String route2 = toStop + "-" + fromStop;

        if (properties.getFares().containsKey(route1)) {
            return Money.of(properties.getFares().get(route1));
        } else if (properties.getFares().containsKey(route2)) {
            return Money.of(properties.getFares().get(route2));
        }

        throw new IllegalArgumentException(
                "System Error: No fare configured for route between " + fromStop + " and " + toStop);
    }

    /**
     * Determines the maximum possible charge from a given origin stop.
     */
    public Money getMaxFareFrom(String originStop) {
        if (!properties.getMaxFares().containsKey(originStop)) {
            throw new IllegalArgumentException("System Error: Unknown origin stop for max fare: " + originStop);
        }
        return Money.of(properties.getMaxFares().get(originStop));
    }
}
