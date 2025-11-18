package com.jakub.energy.carbonintensity;

import com.jakub.energy.carbonintensity.model.CarbonIntensityGenerationResponse;
import com.jakub.energy.carbonintensity.model.GenerationInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class CarbonIntensityApiService implements CarbonIntensityApiFacade {

    private static final String API_BASE_URL = "https://api.carbonintensity.org.uk/generation/";
    private static final String URL_TEMPLATE = API_BASE_URL + "%s/%s";

    private final RestTemplate restTemplate;

    @Override
    public List<GenerationInterval> getCarbonIntensityGenerationData(LocalDateTime from, LocalDateTime to) {
        var url = String.format(URL_TEMPLATE, from, to);
        try {
            var response = restTemplate.getForObject(url, CarbonIntensityGenerationResponse.class);
            return Optional.ofNullable(response)
                    .map(CarbonIntensityGenerationResponse::data)
                    .orElse(List.of());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch generation data", e);
        }
    }
}
