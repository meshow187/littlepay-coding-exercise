# Littlepay Transit Fare Calculator

This is a Java Spring Boot command-line application built for the Littlepay coding exercise. It processes a chronological log of transit taps (`taps.csv`) and generates a calculated summary of completed, incomplete, and cancelled trips (`trips.csv`).

## Prerequisites
* Java 17 (or higher)
* Maven (wrapper included, so no local Maven installation is required)

## How to Run the Application

The application is built to run as a standalone jar file. 

1. **Build the project:**
   Open your terminal in the root directory of the project and run:
   ```bash
   ./mvnw clean package
   ```

2. **Run the processor:**
   By default, the application looks for taps.csv in the current working directory and will output trips.csv to the same location.
   ```bash
   java -jar target/transit-calculator-0.0.1-SNAPSHOT.jar
   ```

3. **Optional: Custom file paths**
   You can pass absolute file paths as arguments if your input file is located elsewhere:
   ```bash
   java -jar target/transit-calculator-0.0.1-SNAPSHOT.jar /path/to/input.csv /path/to/output.csv
   ```

## Running the Tests

A comprehensive JUnit 5 test harness is included to validate the business logic, pricing matrix, and state machine.

To run the test suite:
```bash
./mvnw test
```

## Architecture & Design Decisions

To keep the application robust and production-ready, I structured it using a lightweight Hexagonal Architecture (Ports and Adapters).

**Core Domain:** The core entities (Tap, Trip) are built using immutable Java record types to guarantee thread safety. Financial amounts are encapsulated in a Money value object wrapping BigDecimal to prevent the floating-point precision loss that comes with primitive double types.

**Separation of Concerns:** The business logic (TripCalculator) operates strictly on pure Java domain models. It has zero knowledge of Spring or OpenCSV.

**Infrastructure Layer:** The reading and writing of CSV files are pushed to the outer boundary (CsvFileAdapter), which maps the raw strings to DTOs before translating them into the immutable domain models. This prevents infrastructure details from leaking into the business rules.

## Assumptions Made

While building this solution, I made a few practical assumptions based on standard batch processing and transit rules:

1. **Memory constraints:** I assumed the input file is reasonably sized and can fit into memory for grouping and sorting. If we were processing gigabytes of data, this would need to be refactored into a streaming architecture (e.g., Spring Batch).

2. **Chronological disorder:** I assumed the CSV rows might not be perfectly ordered by time. The system groups taps by PAN and explicitly sorts them by DateTimeUTC before evaluating the trip states.

3. **Orphaned OFF taps:** If a user taps OFF without a preceding ON tap, standard transit logic dictates we cannot determine origin, so the system safely ignores the orphaned tap rather than crashing.

4. **Data cleanliness:** The provided example data contained leading spaces (e.g., `22-01-2018`). I assumed real-world data would be similarly messy, so I added defensive trimming logic to the CSV adapter to prevent DateTimeParseException errors before the data reaches the core domain.

