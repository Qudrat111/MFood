package com.mfood.bot.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "edamam")
public class EdamamProperties {
    private String appId;
    private String appKey;
    private String foodDbBaseUrl = "https://api.edamam.com";
    private String visionBaseUrl = "https://api.edamam.com";
}
