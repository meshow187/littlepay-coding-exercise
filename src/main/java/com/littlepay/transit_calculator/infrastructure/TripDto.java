package com.littlepay.transit_calculator.infrastructure;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDto {

    @CsvBindByPosition(position = 0)
    private String started;

    @CsvBindByPosition(position = 1)
    private String finished;

    @CsvBindByPosition(position = 2)
    private Long durationSecs;

    @CsvBindByPosition(position = 3)
    private String fromStopId;

    @CsvBindByPosition(position = 4)
    private String toStopId;

    @CsvBindByPosition(position = 5)
    private String chargeAmount;

    @CsvBindByPosition(position = 6)
    private String companyId;

    @CsvBindByPosition(position = 7)
    private String busId;

    @CsvBindByPosition(position = 8)
    private String pan;

    @CsvBindByPosition(position = 9)
    private String status;
}
