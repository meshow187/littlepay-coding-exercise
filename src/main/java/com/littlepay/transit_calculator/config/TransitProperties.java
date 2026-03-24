package com.littlepay.transit_calculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "transit")
public class TransitProperties {

    private CsvProperties csv = new CsvProperties();
    private Map<String, String> fares;
    private Map<String, String> maxFares;

    @Data
    public static class CsvProperties {
        private String dateFormat;
    }
}