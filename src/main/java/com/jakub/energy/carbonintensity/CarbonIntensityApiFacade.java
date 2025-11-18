package com.jakub.energy.carbonintensity;

import com.jakub.energy.carbonintensity.model.GenerationInterval;

import java.time.LocalDateTime;
import java.util.List;

public interface CarbonIntensityApiFacade {
    public List<GenerationInterval> getCarbonIntensityGenerationData(LocalDateTime from, LocalDateTime to);
}
