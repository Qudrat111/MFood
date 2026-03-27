package com.mfood.bot.application.service;

import com.mfood.bot.domain.enums.Language;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class MessageService {

    private final Map<Language, Properties> messages = new HashMap<>();

    @PostConstruct
    public void init() {
        messages.put(Language.UZ, loadProperties("i18n/messages_uz.properties"));
        messages.put(Language.RU, loadProperties("i18n/messages_ru.properties"));
    }

    public String getMessage(String key, Language language) {
        Properties props = messages.getOrDefault(language, messages.get(Language.RU));
        return props.getProperty(key, key);
    }

    public String getMessage(String key, Language language, Object... args) {
        String template = getMessage(key, language);
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            log.warn("Failed to format message key={}: {}", key, e.getMessage());
            return template;
        }
    }

    private Properties loadProperties(String resourcePath) {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                log.info("Loaded i18n properties from {}", resourcePath);
            } else {
                log.warn("Could not find i18n file: {}", resourcePath);
            }
        } catch (IOException e) {
            log.error("Failed to load i18n properties from {}: {}", resourcePath, e.getMessage());
        }
        return props;
    }
}
