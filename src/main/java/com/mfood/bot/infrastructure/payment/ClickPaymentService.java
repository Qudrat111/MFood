package com.mfood.bot.infrastructure.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfood.bot.domain.enums.Language;
import com.mfood.bot.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickPaymentService {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public String generateInvoicePayload(Long telegramId, String period) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("telegramId", telegramId);
        payload.put("period", period);
        payload.put("timestamp", System.currentTimeMillis());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to generate invoice payload: {}", e.getMessage());
            return "{\"telegramId\":" + telegramId + ",\"period\":\"" + period + "\"}";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseInvoicePayload(String payload) {
        try {
            return objectMapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse invoice payload: {}", e.getMessage());
            return Map.of();
        }
    }

    public String buildInvoiceTitle(Language language) {
        return messageService.getMessage("subscription.title", language);
    }

    public String buildInvoiceDescription(Language language) {
        return messageService.getMessage("subscription.inactive", language);
    }
}
