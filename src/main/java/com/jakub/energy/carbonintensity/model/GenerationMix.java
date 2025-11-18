package com.jakub.energy.carbonintensity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record GenerationMix(
    @NotNull
    String fuel,
    @JsonProperty("perc")
    @NotNull
    double percentage
){}
