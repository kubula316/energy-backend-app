package com.jakub.energy.service;

import com.jakub.energy.dto.DailyEnergyMixDto;
import com.jakub.energy.dto.OptimalChargingWindowDto;

import java.util.List;

public interface EnergyService {
    List<DailyEnergyMixDto> getThreeDaysEnergyMix();
    OptimalChargingWindowDto getOptimalChargingWindow(int durationHours);
}
