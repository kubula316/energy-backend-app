package com.jakub.energy.dto;

import java.time.LocalDateTime;

public record OptimalChargingWindowDto (
        LocalDateTime startTime,
        LocalDateTime endTime,
        double averageCleanEnergyPercentage
){}
