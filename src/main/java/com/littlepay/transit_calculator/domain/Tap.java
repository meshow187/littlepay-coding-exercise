package com.littlepay.transit_calculator.domain;

import java.time.LocalDateTime;

public record Tap(Long id,
                  LocalDateTime dateTimeUTC,
                  TapType tapType,
                  String stopId,
                  String companyId,
                  String busId,
                  String pan) {

}
