package com.jakub.energy.controller;

import com.jakub.energy.dto.DailyEnergyMixDto;
import com.jakub.energy.dto.OptimalChargingWindowDto;
import com.jakub.energy.service.EnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
@CrossOrigin
public class EnergyController {

    private final EnergyService energyService;

    @GetMapping
    public ResponseEntity<List<DailyEnergyMixDto>> getEnergyMix() {
        List<DailyEnergyMixDto> energyMix = energyService.getThreeDaysEnergyMix();
        return ResponseEntity.ok(energyMix);
    }

    @GetMapping("/optimal-charging")
    public ResponseEntity<OptimalChargingWindowDto> getOptimalChargingWindow(int durationHours){
        OptimalChargingWindowDto window = energyService.getOptimalChargingWindow(durationHours);
        return ResponseEntity.ok(window);
    }

}
