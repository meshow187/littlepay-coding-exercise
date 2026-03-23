package com.littlepay.transit_calculator.runner;

import com.littlepay.transit_calculator.domain.Tap;
import com.littlepay.transit_calculator.domain.Trip;
import com.littlepay.transit_calculator.infrastructure.CsvFileAdapter;
import com.littlepay.transit_calculator.service.TripCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final CsvFileAdapter csvFileAdapter;
    private final TripCalculator tripCalculator;

    public AppRunner(CsvFileAdapter csvFileAdapter, TripCalculator tripCalculator) {
        this.csvFileAdapter = csvFileAdapter;
        this.tripCalculator = tripCalculator;
    }

    @Override
    public void run(String... args) {
        // Allow overriding file paths via command line arguments, default to requirements
        String inputFile = args.length > 0 ? args[0] : "taps.csv";
        String outputFile = args.length > 1 ? args[1] : "trips.csv";

        logger.info("Starting Littlepay Transit Calculator...");
        logger.info("Reading input from: {}", inputFile);

        try {
            List<Tap> taps = csvFileAdapter.readTaps(inputFile);
            logger.info("Successfully read {} tap records.", taps.size());

            List<Trip> trips = tripCalculator.calculateTrips(taps);
            logger.info("Successfully calculated {} trips.", trips.size());

            csvFileAdapter.writeTrips(outputFile, trips);
            logger.info("Successfully wrote output to: {}", outputFile);
            logger.info("Processing complete!");

        } catch (Exception e) {
            logger.error("An error occurred during processing: {}", e.getMessage(), e);
        }
    }
}
