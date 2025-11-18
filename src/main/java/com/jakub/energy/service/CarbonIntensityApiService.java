package com.jakub.energy.service;

import com.jakub.energy.model.GenerationInterval;

import java.time.LocalDateTime;
import java.util.List;

public interface CarbonIntensityApiService {
    public List<GenerationInterval> getCarbonIntensityGenerationData(LocalDateTime from, LocalDateTime to);
}
