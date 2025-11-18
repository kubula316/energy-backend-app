package com.jakub.energy.model;

import java.time.LocalDateTime;
import java.util.List;

public record GenerationInterval(
    LocalDateTime from,
    LocalDateTime to,
    List<GenerationMix> generationMix
) { }
