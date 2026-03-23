package com.littlepay.transit_calculator.service;

import com.littlepay.transit_calculator.domain.Money;
import com.littlepay.transit_calculator.domain.Tap;
import com.littlepay.transit_calculator.domain.TapType;
import com.littlepay.transit_calculator.domain.Trip;
import com.littlepay.transit_calculator.domain.TripStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TripCalculator {

    private final FareMatrix fareMatrix;

    public TripCalculator(FareMatrix fareMatrix) {
        this.fareMatrix = fareMatrix;
    }

    public List<Trip> calculateTrips(List<Tap> allTaps) {
        List<Trip> completedTrips = new ArrayList<>();

        // Group taps by PAN (Primary Account Number)
        Map<String, List<Tap>> tapsByPan = allTaps.stream()
                .collect(Collectors.groupingBy(Tap::pan));

        // Process each passenger's journey independently
        for (List<Tap> panTaps : tapsByPan.values()) {

            // Ensure taps are processed in chronological order
            panTaps.sort(Comparator.comparing(Tap::dateTimeUTC));

            Tap pendingOnTap = null;

            for (Tap currentTap : panTaps) {
                if (currentTap.tapType() == TapType.ON) {
                    // If we already have a pending ON tap, the previous one is INCOMPLETE
                    if (pendingOnTap != null) {
                        completedTrips.add(buildIncompleteTrip(pendingOnTap));
                    }
                    pendingOnTap = currentTap;
                } else if (currentTap.tapType() == TapType.OFF) {
                    if (pendingOnTap != null) {
                        // We have a matching ON and OFF pair
                        completedTrips.add(buildCompletedOrCancelledTrip(pendingOnTap, currentTap));
                        pendingOnTap = null; // Reset state
                    }
                    // Edge case: An OFF tap without an ON tap is ignored per standard transit rules
                }
            }

            // After processing all taps for this PAN, check if the last tap was an ON tap
            if (pendingOnTap != null) {
                completedTrips.add(buildIncompleteTrip(pendingOnTap));
            }
        }

        return completedTrips;
    }

    private Trip buildCompletedOrCancelledTrip(Tap tapOn, Tap tapOff) {
        long durationSecs = Duration.between(tapOn.dateTimeUTC(), tapOff.dateTimeUTC()).getSeconds();
        boolean isCancelled = tapOn.stopId().equals(tapOff.stopId());

        TripStatus status = isCancelled ? TripStatus.CANCELLED : TripStatus.COMPLETED;
        Money charge = fareMatrix.getFare(tapOn.stopId(), tapOff.stopId());

        return new Trip(
                tapOn.dateTimeUTC(),
                tapOff.dateTimeUTC(),
                durationSecs,
                tapOn.stopId(),
                tapOff.stopId(),
                charge,
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                status
        );
    }

    private Trip buildIncompleteTrip(Tap tapOn) {
        Money maxCharge = fareMatrix.getMaxFareFrom(tapOn.stopId());

        return new Trip(
                tapOn.dateTimeUTC(),
                null, // No finish time
                0L,   // No duration
                tapOn.stopId(),
                null, // No destination
                maxCharge,
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                TripStatus.INCOMPLETE
        );
    }
}
