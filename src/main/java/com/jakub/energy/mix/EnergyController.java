package com.jakub.energy.mix;

import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
class EnergyController {

    private final EnergyFacade energyService;

    @GetMapping("/mix")
    public List<DailyEnergyMixDto> getEnergyMix() {
        return energyService.getThreeDaysEnergyMix();
    }

    @GetMapping("/optimal-charging")
    public OptimalChargingWindowDto getOptimalChargingWindow(@RequestParam(value = "duration", defaultValue = "3")
                                                             @Min(value = 1, message = "Duration value must be at least 1 hour")
                                                             @Max(value = 6, message = "Duration value must be at most 6 hours")
                                                             int durationHours) {
        return energyService.getOptimalChargingWindow(durationHours);
    }

}
