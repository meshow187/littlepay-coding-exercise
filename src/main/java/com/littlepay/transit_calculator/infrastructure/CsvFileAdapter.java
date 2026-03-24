package com.littlepay.transit_calculator.infrastructure;

import com.littlepay.transit_calculator.domain.Tap;
import com.littlepay.transit_calculator.domain.TapType;
import com.littlepay.transit_calculator.domain.Trip;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvFileAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public List<Tap> readTaps(String inputFilePath) {
        try (Reader reader = new FileReader(inputFilePath)) {
            List<TapDto> dtos = new CsvToBeanBuilder<TapDto>(reader)
                    .withType(TapDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            return dtos.stream()
                    .map(dto -> {
                        try {
                            return mapToDomain(dto);
                        } catch (Exception e) {
                            System.err
                                    .println("ERROR: Skipping malformed row ID " + dto.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(tap -> tap != null) // Remove the failed rows
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Fatal error reading CSV file: " + inputFilePath, e);
        }
    }

    public void writeTrips(String outputFilePath, List<Trip> trips) {
        try (Writer writer = new FileWriter(outputFilePath)) {

            // Manually write the header since @CsvBindByPosition doesn't generate headers
            // by default
            writer.write(
                    "Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status\n");

            List<TripDto> dtos = trips.stream().map(this::mapToDto).toList();

            StatefulBeanToCsv<TripDto> beanToCsv = new StatefulBeanToCsvBuilder<TripDto>(writer)
                    .withApplyQuotesToAll(false)
                    .build();

            beanToCsv.write(dtos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write output CSV file: " + outputFilePath, e);
        }
    }

    // --- Mapping Methods ---

    private Tap mapToDomain(TapDto dto) {
        // Trim the raw string and parse it safely
        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDateTimeUTC().trim(), FORMATTER);

        return new Tap(
                dto.getId(),
                parsedDate,
                TapType.valueOf(dto.getTapType().trim().toUpperCase()),
                dto.getStopId().trim(),
                dto.getCompanyId().trim(),
                dto.getBusId().trim(),
                dto.getPan().trim());
    }

    private TripDto mapToDto(Trip trip) {
        return TripDto.builder()
                .started(trip.started().format(FORMATTER))
                .finished(trip.finished() != null ? trip.finished().format(FORMATTER) : "")
                .durationSecs(trip.durationSecs())
                .fromStopId(trip.fromStopId())
                .toStopId(trip.toStopId() != null ? trip.toStopId() : "")
                .chargeAmount(trip.chargeAmount().format())
                .companyId(trip.companyId())
                .busId(trip.busId())
                .pan(trip.pan())
                .status(trip.status().name())
                .build();
    }
}
