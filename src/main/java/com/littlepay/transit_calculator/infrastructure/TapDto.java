package com.littlepay.transit_calculator.infrastructure;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TapDto {

    @CsvBindByName(column = "ID", required = true)
    private Long id;

    // Changed to String, removed @CsvDate
    @CsvBindByName(column = "DateTimeUTC", required = true)
    private String dateTimeUTC;

    @CsvBindByName(column = "TapType", required = true)
    private String tapType;

    @CsvBindByName(column = "StopId", required = true)
    private String stopId;

    @CsvBindByName(column = "CompanyId", required = true)
    private String companyId;

    @CsvBindByName(column = "BusID", required = true)
    private String busId;

    @CsvBindByName(column = "PAN", required = true)
    private String pan;
}
