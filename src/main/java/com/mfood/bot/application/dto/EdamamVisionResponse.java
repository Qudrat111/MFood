package com.mfood.bot.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdamamVisionResponse {

    @JsonProperty("hints")
    private List<Hint> hints;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hint {
        @JsonProperty("food")
        private Food food;

        @JsonProperty("measures")
        private List<Measure> measures;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Food {
        @JsonProperty("foodId")
        private String foodId;

        @JsonProperty("label")
        private String label;

        @JsonProperty("nutrients")
        private Map<String, Double> nutrients;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Measure {
        @JsonProperty("label")
        private String label;

        @JsonProperty("weight")
        private Double weight;
    }
}
