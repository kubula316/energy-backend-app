package com.jakub.energy.mix;

import com.jakub.energy.mix.exception.ExternalDataFetchException;
import com.jakub.energy.mix.exception.MixExceptionHandler;
import com.jakub.energy.mix.exception.OptimalChargingWindowNotFoundException;
import com.jakub.energy.mix.model.DailyEnergyMixDto;
import com.jakub.energy.mix.model.OptimalChargingWindowDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EnergyController.class)
@Import(MixExceptionHandler.class)
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnergyFacade energyFacade;

    @Test
    @DisplayName("GET /energy/optimal-charging - Should return 200")
    void shouldReturnOptimalChargingWindow() throws Exception {

        ZonedDateTime start = LocalDateTime.of(2023, 10, 1, 12, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime end = LocalDateTime.of(2023, 10, 3, 12, 0).atZone(ZoneOffset.UTC);

        OptimalChargingWindowDto optimalChargingWindow = new OptimalChargingWindowDto(start.toLocalDateTime(), end.toLocalDateTime(), 85.5);

        when(energyFacade.getOptimalChargingWindow(3)).thenReturn(optimalChargingWindow);

        mockMvc.perform(get("/energy/optimal-charging")
                        .param("duration", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(85.5))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists());
    }

    @Test
    @DisplayName("GET /energy/optimal-charging - Should return 400")
    void shouldReturnBadRequestForInvalidDuration() throws Exception {

        mockMvc.perform(get("/energy/optimal-charging")
                        .param("duration", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/energy/optimal-charging")
                        .param("duration", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /energy/optimal-charging - Should return 404")
    void shouldReturnNotFoundWhenWindowNotFound() throws Exception {
        when(energyFacade.getOptimalChargingWindow(3))
                .thenThrow(new OptimalChargingWindowNotFoundException("Not enough data"));

        mockMvc.perform(get("/energy/optimal-charging")
                        .param("duration", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Not enough data"));
    }

    @Test
    @DisplayName("GET /energy/mix - Should return 200")
    void shouldReturnEnergyMix() throws Exception {

        DailyEnergyMixDto mix1 = new DailyEnergyMixDto(LocalDate.now(), Map.of("hydro", 10.0), 50.0);
        DailyEnergyMixDto mix2 = new DailyEnergyMixDto(LocalDate.now().plusDays(1), Map.of("solar", 30.0), 50.0);
        DailyEnergyMixDto mix3 = new DailyEnergyMixDto(LocalDate.now().plusDays(2), Map.of("wind", 80.0), 80.0);

        when(energyFacade.getThreeDaysEnergyMix()).thenReturn(List.of(mix1, mix2, mix3));

        mockMvc.perform(get("/energy/mix")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value(50.0));
    }

    @Test
    @DisplayName("GET /energy/mix - Should return 502 (API fails)")
    void shouldReturnBadGatewayWhenExternalApiFails() throws Exception {
        when(energyFacade.getThreeDaysEnergyMix())
                .thenThrow(new ExternalDataFetchException("API failure", new RuntimeException()));

        mockMvc.perform(get("/energy/mix")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("API failure"));
    }


}
