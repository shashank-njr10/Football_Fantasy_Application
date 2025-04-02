package com.example.fantasy_football_backend.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FootballApiConfig {

    @Value("${app.api.football-data.key}")
    private String apiKey;

    @Bean
    public RestTemplate footballApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("X-Auth-Token", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });

        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}