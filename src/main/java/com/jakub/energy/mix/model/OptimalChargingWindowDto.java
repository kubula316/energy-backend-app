package com.jakub.energy.mix.model;

import java.time.LocalDateTime;

public record OptimalChargingWindowDto (
        LocalDateTime startTime,
        LocalDateTime endTime,
        double averageCleanEnergyPercentage
){}
