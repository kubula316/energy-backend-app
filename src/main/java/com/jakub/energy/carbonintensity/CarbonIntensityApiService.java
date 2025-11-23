package com.jakub.energy.carbonintensity;

import com.jakub.energy.carbonintensity.model.CarbonIntensityGenerationResponse;
import com.jakub.energy.carbonintensity.model.GenerationInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class CarbonIntensityApiService implements CarbonIntensityApiFacade {

    @Value("${api.carbon-intensity.base-url}")
    private String baseUrl;

    private static final String URL_TEMPLATE = "%s%s/%s";

    private final RestTemplate restTemplate;

    @Override
    public List<GenerationInterval> getCarbonIntensityGenerationData(LocalDateTime from, LocalDateTime to) {
        String url = URL_TEMPLATE.formatted(baseUrl, from, to);
        try {
            CarbonIntensityGenerationResponse response = restTemplate.getForObject(url, CarbonIntensityGenerationResponse.class);
            return Optional.ofNullable(response)
                    .map(CarbonIntensityGenerationResponse::data)
                    .orElse(List.of());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch generation data", e);
        }
    }
}
