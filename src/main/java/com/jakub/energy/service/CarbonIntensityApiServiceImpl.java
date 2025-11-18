package com.jakub.energy.service;

import com.jakub.energy.model.CarbonIntensityApiResponse;
import com.jakub.energy.model.GenerationInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarbonIntensityApiServiceImpl implements CarbonIntensityApiService{

    private static final String API_BASE_URL = "https://api.carbonintensity.org.uk/generation/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    private final RestTemplate restTemplate;

    @Override
    public List<GenerationInterval> getCarbonIntensityGenerationData(LocalDateTime from, LocalDateTime to) {
        String url = API_BASE_URL + from.format(DateTimeFormatter.ISO_DATE_TIME) + "/" + to.format(DateTimeFormatter.ISO_DATE_TIME);
        try {
            CarbonIntensityApiResponse response = restTemplate.getForObject(url, CarbonIntensityApiResponse.class);
            if (response != null && response.data() != null) {
                return response.data();
            }
        }catch (Exception e){
            throw new RuntimeException("Failed to fetch generation data", e);
        }
        return List.of();
    }
}
