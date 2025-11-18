package com.jakub.energy.mix;

import com.jakub.energy.carbonintensity.CarbonIntensityApiFacade;
import com.jakub.energy.carbonintensity.model.GenerationInterval;
import com.jakub.energy.carbonintensity.model.GenerationMix;
import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EnergyService implements EnergyFacade {

    private final CarbonIntensityApiFacade carbonIntensityFacade;

    private static final List<String> CLEAN_ENERGY_SOURCES = List.of("biomass", "nuclear", "hydro", "wind", "solar");


    @Override
    public List<DailyEnergyMixDto> getThreeDaysEnergyMix() {
        ZonedDateTime from = LocalDate.now().atTime(0,1).atZone(ZoneOffset.UTC);
        ZonedDateTime to = LocalDate.now().plusDays(2).atTime(LocalTime.MAX).atZone(ZoneOffset.UTC);

        try {
            List<GenerationInterval> allIntervals = carbonIntensityFacade.getCarbonIntensityGenerationData(from.toLocalDateTime(), to.toLocalDateTime());

            Map<LocalDate, List<GenerationInterval>> intervalsByDate = allIntervals.stream()
                    .collect(Collectors.groupingBy(interval -> interval.from().toLocalDate()));

            return intervalsByDate.entrySet().stream()
                    .map(entry -> calculateDailyAverage(entry.getKey(), entry.getValue()))
                    //Sort for frontend convenience
                    .sorted(Comparator.comparing(DailyEnergyMixDto::date))
                    .collect(Collectors.toList());
        }catch (Exception e){
            throw new RuntimeException("Failed to fetch three-day energy mix data", e);
        }

    }

    @Override
    public OptimalChargingWindowDto getOptimalChargingWindow(int durationHours) {
            ZonedDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneOffset.UTC);
            ZonedDateTime dayAfterTomorrow = LocalDate.now().plusDays(2).atTime(LocalTime.MAX).atZone(ZoneOffset.UTC);

            List<GenerationInterval> intervals = carbonIntensityFacade.getCarbonIntensityGenerationData(tomorrow.toLocalDateTime(), dayAfterTomorrow.toLocalDateTime());

            int intervalNeeded = durationHours *2;
            double maxCleanEnergy = 0;
            int optimalStartIndex = 0;

            for (int i=0; i <= intervals.size() - intervalNeeded; i++){
                double averageCleanEnergy = calculateAverageCleanEnergy(intervals.subList(i, i + intervalNeeded));
                if (averageCleanEnergy > maxCleanEnergy){
                    maxCleanEnergy = averageCleanEnergy;
                    optimalStartIndex = i;
                }
            }

            GenerationInterval startInterval = intervals.get(optimalStartIndex);
            GenerationInterval endInterval = intervals.get(optimalStartIndex + intervalNeeded -1);
            return new OptimalChargingWindowDto(
                    startInterval.from(),
                    endInterval.to(),
                    maxCleanEnergy
            );




    }

    private double calculateAverageCleanEnergy(List<GenerationInterval> generationIntervals) {
        double totalCleanEnergy = 0;
        int count = 0;

        for (GenerationInterval interval : generationIntervals){
            if (interval.generationMix() != null){
                double cleanPercentage = interval.generationMix().stream()
                        .filter(mix -> CLEAN_ENERGY_SOURCES.contains(mix.fuel()))
                        .mapToDouble(GenerationMix::percentage)
                        .sum();
                totalCleanEnergy += cleanPercentage;
                count++;
            }
        }
        return count > 0 ? totalCleanEnergy / count : 0;
    }

    private DailyEnergyMixDto calculateDailyAverage(LocalDate date, List<GenerationInterval> intervals) {
        Map<String,Double> totalPercentages = new HashMap<>();
        int count = intervals.size();


        intervals.stream()
                .flatMap(interval -> interval.generationMix().stream())
                .forEach(mix -> totalPercentages.merge(mix.fuel(), mix.percentage(), Double::sum));

        Map<String, Double> averagePercentages = totalPercentages.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() / count
                ));

        double cleanEnergyPercentage = calculateCleanEnergyPercentage(averagePercentages);

        return new DailyEnergyMixDto(
                date,
                averagePercentages,
                cleanEnergyPercentage
        );
    }

    private double calculateCleanEnergyPercentage(Map<String, Double> averagePercentages) {
        return averagePercentages.entrySet().stream()
                .filter(entry -> CLEAN_ENERGY_SOURCES.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
}
