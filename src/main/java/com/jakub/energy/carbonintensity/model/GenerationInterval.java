package com.jakub.energy.carbonintensity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record GenerationInterval(
        LocalDateTime from,
        LocalDateTime to,
        @NotNull
        @JsonProperty("generationmix")
        List<GenerationMix> generationMix
) {
}
