package com.jakub.energy.carbonintensity.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CarbonIntensityGenerationResponse(
    List<GenerationInterval> data
) {
}
