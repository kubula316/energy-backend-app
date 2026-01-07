package com.jakub.energy.mix;

import com.jakub.energy.carbonintensity.CarbonIntensityApiFacade;
import com.jakub.energy.carbonintensity.model.GenerationInterval;
import com.jakub.energy.carbonintensity.model.GenerationMix;
import com.jakub.energy.mix.exception.ExternalDataFetchException;
import com.jakub.energy.mix.model.CleanEnergySource;
import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
@RequiredArgsConstructor
class EnergyService implements EnergyFacade {

    private final CarbonIntensityApiFacade carbonIntensityFacade;

    @Override
    public List<DailyEnergyMixDto> getThreeDaysEnergyMix() {
        ZonedDateTime from = LocalDate.now().atTime(0, 1).atZone(ZoneOffset.UTC);
        ZonedDateTime to = LocalDate.now().plusDays(2).atTime(LocalTime.MAX).atZone(ZoneOffset.UTC);

        try {
            List<GenerationInterval> allIntervals = carbonIntensityFacade.getCarbonIntensityGenerationData(from.toLocalDateTime(), to.toLocalDateTime());

            Map<LocalDate, List<GenerationInterval>> intervalsByDate = allIntervals.stream()
                    .collect(Collectors.groupingBy(interval -> interval.from().toLocalDate()));

            return intervalsByDate.entrySet().stream()
                    .map(entry -> calculateDailyAverage(entry.getKey(), entry.getValue()))
                    //Sort for frontend convenience
                    .sorted(comparing(DailyEnergyMixDto::date))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ExternalDataFetchException("Failed to fetch three-day energy mix data", e);
        }

    }

    @Override
    public OptimalChargingWindowDto getOptimalChargingWindow(int durationHours) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime end = now.plusHours(48);

        List<GenerationInterval> intervals;
        try {
            intervals = carbonIntensityFacade.getCarbonIntensityGenerationData(now.toLocalDateTime(), end.toLocalDateTime());
        } catch (Exception e) {
            throw new ExternalDataFetchException("Failed to fetch energy data for optimal window calculation", e);
        }

        int intervalsNeeded = durationHours * 2;

        List<Double> cleanScores = intervals.stream()
                .map(this::calculateCleanPercentageForInterval)
                .toList();

        double currentWindowSum = 0;
        for (int i = 0; i < intervalsNeeded; i++) {
            currentWindowSum += cleanScores.get(i);
        }
        double maxWindowSum = currentWindowSum;
        int optimalStartIndex = 0;

        for (int i = 1; i <= cleanScores.size() - intervalsNeeded; i++) {
            double outgoing = cleanScores.get(i - 1);
            double incoming = cleanScores.get(i + intervalsNeeded - 1);

            currentWindowSum = currentWindowSum - outgoing + incoming;

            if (currentWindowSum > maxWindowSum) {
                maxWindowSum = currentWindowSum;
                optimalStartIndex = i;
            }
        }

        GenerationInterval bestStart = intervals.get(optimalStartIndex);
        GenerationInterval bestEnd = intervals.get(optimalStartIndex + intervalsNeeded - 1);

        return new OptimalChargingWindowDto(
                bestStart.from(),
                bestEnd.to(),
                maxWindowSum / intervalsNeeded
        );
    }

    private double calculateCleanPercentageForInterval(GenerationInterval interval) {
        if (interval.generationMix() == null) return 0.0;
        return interval.generationMix().stream()
                .filter(mix -> CleanEnergySource.isClean(mix.fuel()))
                .mapToDouble(GenerationMix::percentage)
                .sum();
    }

    private DailyEnergyMixDto calculateDailyAverage(LocalDate date, List<GenerationInterval> intervals) {
        Map<String, Double> totalPercentages = new HashMap<>();
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
                .filter(entry -> CleanEnergySource.isClean(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
}
