package com.jakub.energy.mix.model;

import java.time.LocalDate;
import java.util.Map;

public record DailyEnergyMixDto (
        LocalDate date,
        Map<String,Double> averageGenerationMix,
        double cleanEnergyPercentage
){}