package com.jakub.energy.mix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakub.energy.carbonintensity.model.CarbonIntensityGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class EnergyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    private CarbonIntensityGenerationResponse apiResponse;

    @BeforeEach
    void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String json = Files.readString(Path.of("src/test/resources/GenerationMixExampleData.json"));

        apiResponse = mapper.readValue(
                json,
                CarbonIntensityGenerationResponse.class
        );
    }

    @Test
    @DisplayName("Integration Test - GET /api/energy/mix - Should return three days energy mix")
    void shouldReturnThreeDaysEnergyMix() throws Exception {

        when(restTemplate.getForObject(anyString(), eq(CarbonIntensityGenerationResponse.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/energy/mix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].averageGenerationMix").isMap())
                .andExpect(jsonPath("$[0].date").value("2025-11-19"))
                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value("79.3375"));
    }

    @Test
    @DisplayName("Integration Test - GET /energy/optimal-charging - Should return optimal charging window")
    void shouldReturnOptimalChargingWindow() throws Exception {

        when(restTemplate.getForObject(anyString(), eq(CarbonIntensityGenerationResponse.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/energy/optimal-charging")
                        .param("duration", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("2025-11-19T02:00:00"))
                .andExpect(jsonPath("$.endTime").value("2025-11-19T04:00:00"))
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(79.825));
    }
}
