package com.mfood.bot;

import com.mfood.bot.presentation.bot.MFoodBot;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "telegram.bot.token=test_token",
        "telegram.bot.username=test_bot",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.data.redis.url=redis://localhost:6379",
        "spring.data.redis.repositories.enabled=false"
})
class MFoodApplicationTests {

    // Mock the bot to prevent actual Telegram API registration during tests
    @MockBean
    MFoodBot mFoodBot;

    @Test
    void contextLoads() {
        // Verifies Spring context loads without errors
    }
}
