package com.jakub.energy.service;

import com.jakub.energy.dto.DailyEnergyMixDto;
import com.jakub.energy.dto.OptimalChargingWindowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnergyServiceImpl implements EnergyService{

    private final CarbonIntensityApiService carbonIntensityApiService;


    @Override
    public List<DailyEnergyMixDto> getThreeDaysEnergyMix() {
        return List.of();
    }

    @Override
    public OptimalChargingWindowDto getOptimalChargingWindow(int durationHours) {
        return null;
    }
}
