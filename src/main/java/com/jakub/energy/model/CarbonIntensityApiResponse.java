package com.jakub.energy.model;

import java.util.List;

public record CarbonIntensityApiResponse(
    List<GenerationInterval> data
) {}
