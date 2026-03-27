package com.mfood.bot.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("edamamFoodWebClient")
    public WebClient edamamFoodWebClient(EdamamProperties edamamProperties) {
        return WebClient.builder()
                .baseUrl(edamamProperties.getFoodDbBaseUrl())
                .build();
    }

    @Bean("edamamVisionWebClient")
    public WebClient edamamVisionWebClient(EdamamProperties edamamProperties) {
        return WebClient.builder()
                .baseUrl(edamamProperties.getVisionBaseUrl())
                .build();
    }
}
