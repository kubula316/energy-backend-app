package com.jakub.energy.mix;

import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;

import java.util.List;

public interface EnergyFacade {
    List<DailyEnergyMixDto> getThreeDaysEnergyMix();
    OptimalChargingWindowDto getOptimalChargingWindow(int durationHours);
}
