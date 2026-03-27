package com.mfood.bot.infrastructure.edamam;

import com.mfood.bot.application.dto.EdamamFoodSearchResponse;
import com.mfood.bot.application.dto.FoodItemDto;
import com.mfood.bot.infrastructure.config.EdamamProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EdamamClient {

    private static final String ENERC_KCAL = "ENERC_KCAL";
    private static final String PROCNT = "PROCNT";
    private static final String FAT = "FAT";
    private static final String CHOCDF = "CHOCDF";

    private final WebClient foodWebClient;
    private final WebClient visionWebClient;
    private final EdamamProperties edamamProperties;

    public EdamamClient(
            @Qualifier("edamamFoodWebClient") WebClient foodWebClient,
            @Qualifier("edamamVisionWebClient") WebClient visionWebClient,
            EdamamProperties edamamProperties) {
        this.foodWebClient = foodWebClient;
        this.visionWebClient = visionWebClient;
        this.edamamProperties = edamamProperties;
    }

    public Mono<List<FoodItemDto>> searchFood(String query, String appId, String appKey) {
        log.debug("Searching food: query={}", query);
        return foodWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/food-database/v2/parser")
                        .queryParam("ingr", query)
                        .queryParam("app_id", appId)
                        .queryParam("app_key", appKey)
                        .build())
                .retrieve()
                .bodyToMono(EdamamFoodSearchResponse.class)
                .map(this::mapToFoodItems)
                .onErrorResume(e -> {
                    log.error("Edamam searchFood error for query={}: {}", query, e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }

    public Mono<Optional<FoodItemDto>> analyzeByBarcode(String upc, String appId, String appKey) {
        log.debug("Analyzing barcode: upc={}", upc);
        return foodWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/food-database/v2/parser")
                        .queryParam("upc", upc)
                        .queryParam("app_id", appId)
                        .queryParam("app_key", appKey)
                        .build())
                .retrieve()
                .bodyToMono(EdamamFoodSearchResponse.class)
                .map(response -> {
                    List<FoodItemDto> items = mapToFoodItems(response);
                    return items.isEmpty() ? Optional.<FoodItemDto>empty() : Optional.of(items.get(0));
                })
                .onErrorResume(e -> {
                    log.error("Edamam barcode error for upc={}: {}", upc, e.getMessage());
                    return Mono.just(Optional.empty());
                });
    }

    public Mono<List<FoodItemDto>> analyzeImage(String imageUrl, String appId, String appKey) {
        log.debug("Analyzing image: url={}", imageUrl);
        Map<String, String> body = Map.of("url", imageUrl);
        return visionWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/food-database/v2/parser")
                        .queryParam("app_id", appId)
                        .queryParam("app_key", appKey)
                        .build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(EdamamFoodSearchResponse.class)
                .map(this::mapToFoodItems)
                .onErrorResume(e -> {
                    log.error("Edamam analyzeImage error for imageUrl={}: {}", imageUrl, e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }

    private List<FoodItemDto> mapToFoodItems(EdamamFoodSearchResponse response) {
        if (response == null || response.getHints() == null) return Collections.emptyList();
        return response.getHints().stream()
                .filter(h -> h.getFood() != null)
                .map(h -> {
                    EdamamFoodSearchResponse.Food food = h.getFood();
                    Map<String, Double> nutrients = food.getNutrients() != null ? food.getNutrients() : Map.of();
                    return FoodItemDto.builder()
                            .foodId(food.getFoodId())
                            .label(food.getLabel())
                            .calories(nutrients.getOrDefault(ENERC_KCAL, 0.0))
                            .protein(nutrients.getOrDefault(PROCNT, 0.0))
                            .fat(nutrients.getOrDefault(FAT, 0.0))
                            .carbs(nutrients.getOrDefault(CHOCDF, 0.0))
                            .servingSize(100.0)
                            .unit("g")
                            .build();
                })
                .collect(Collectors.toList());
    }
}
