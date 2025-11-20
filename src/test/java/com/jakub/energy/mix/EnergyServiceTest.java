package com.jakub.energy.mix;

import com.jakub.energy.carbonintensity.CarbonIntensityApiFacade;
import com.jakub.energy.carbonintensity.model.GenerationInterval;
import com.jakub.energy.carbonintensity.model.GenerationMix;
import com.jakub.energy.mix.exception.ExternalDataFetchException;
import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private CarbonIntensityApiFacade carbonIntensityFacade;

    @InjectMocks
    private EnergyService energyService;

    @Test
    @DisplayName("getThreeDaysEnergyMix - Should group by date and calculate averages")
    void shouldCalculateThreeDaysMix() {

        ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.UTC);

        GenerationInterval day1_1 = createTestInterval(now, 10.0);
        GenerationInterval day1_2 = createTestInterval(now.plusMinutes(30), 20.0);
        GenerationInterval day1_3 = createTestInterval(now.plusHours(1), 30.0);

        GenerationInterval day2_1 = createTestInterval(now.plusDays(1), 30);
        GenerationInterval day2_2 = createTestInterval(now.plusDays(1).plusMinutes(30), 60.0);
        GenerationInterval day2_3 = createTestInterval(now.plusDays(1).plusHours(1), 30.0);

        GenerationInterval day3_1 = createTestInterval(now.plusDays(2), 30);
        GenerationInterval day3_2 = createTestInterval(now.plusDays(2).plusMinutes(30), 30);
        GenerationInterval day3_3 = createTestInterval(now.plusDays(2).plusHours(1), 30.0);

        when(carbonIntensityFacade.getCarbonIntensityGenerationData(any(), any()))
                .thenReturn(List.of(day1_1, day1_2, day1_3,day2_1,day2_2,day2_3,day3_1,day3_2,day3_3));

        List<DailyEnergyMixDto> result = energyService.getThreeDaysEnergyMix();

        assertNotNull(result);
        assertEquals(3, result.size());

        DailyEnergyMixDto resultDay1 = result.stream()
                .filter(d -> d.date().equals(now.toLocalDate()))
                .findFirst().orElseThrow();
        assertEquals(20.0, resultDay1.cleanEnergyPercentage(), 0.01);

        DailyEnergyMixDto resultDay2 = result.stream()
                .filter(d -> d.date().equals(now.plusDays(1).toLocalDate()))
                .findFirst().orElseThrow();
        assertEquals(40.0, resultDay2.cleanEnergyPercentage(), 0.01);

        DailyEnergyMixDto resultDay3 = result.stream()
                .filter(d -> d.date().equals(now.plusDays(2).toLocalDate()))
                .findFirst().orElseThrow();
        assertEquals(30.0, resultDay3.cleanEnergyPercentage(), 0.01);
    }

    @Test
    @DisplayName("getThreeDaysEnergyMix - Should throw ExternalDataFetchException when API fails")
    void shouldThrowExceptionWhenFetchingMixFails() {

        when(carbonIntensityFacade.getCarbonIntensityGenerationData(any(), any()))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(ExternalDataFetchException.class, () -> energyService.getThreeDaysEnergyMix());
    }

    @Test
    @DisplayName("getOptimalChargingWindow - Should find optimal window based on clean energy")
    void shouldFindOptimalChargingWindow() {

        ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.UTC);

        List<GenerationInterval> intervals = Stream.iterate(0, i -> i + 1)
                .limit(96)
                .map(i -> {
                    double cleanPercentage;
                    if (i >= 30 && i < 36) {
                        cleanPercentage = 80.0;
                    } else if (i >= 60 && i < 66) {
                        cleanPercentage = 70.0;
                    } else {
                        cleanPercentage = 20.0 + (i % 10) * 5; // Varying lower percentages
                    }
                    return createTestInterval(now.plusMinutes(i * 30), cleanPercentage);
                })
                .toList();

        when(carbonIntensityFacade.getCarbonIntensityGenerationData(any(), any()))
                .thenReturn(intervals);

        OptimalChargingWindowDto result = energyService.getOptimalChargingWindow(3);

        assertNotNull(result);
        assertEquals(now.plusHours(15).toLocalDateTime(), result.startTime());
        assertEquals(now.plusHours(18).toLocalDateTime(), result.endTime());
        assertEquals(80.0, result.averageCleanEnergyPercentage(), 0.01);
    }

    @Test
    @DisplayName("getOptimalChargingWindow - Should throw ExternalDataFetchException when API fails")
    void shouldThrowExceptionWhenFetchingOptimalWindowFails() {

        when(carbonIntensityFacade.getCarbonIntensityGenerationData(any(), any()))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(ExternalDataFetchException.class, () -> energyService.getOptimalChargingWindow(2));
    }

    private GenerationInterval createTestInterval(ZonedDateTime from, double cleanPercentage) {

        GenerationMix wind = new GenerationMix("wind", cleanPercentage);
        GenerationMix coal = new GenerationMix("coal", 100.0 - cleanPercentage);

        return new GenerationInterval(
                from.toLocalDateTime(),
                from.plusMinutes(30).toLocalDateTime(),
                List.of(wind, coal)
        );
    }
}
