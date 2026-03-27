# MFood - Telegram Food Calorie Tracking Bot

A production-ready Telegram bot for food calorie tracking, built with Spring Boot 3 / Java 17.

## Features

- 🍎 **Calorie tracking** via photo upload or manual entry (Edamam API)
- 👤 **User onboarding** with BMR/TDEE calculation (Mifflin-St Jeor formula)
- 📊 **Daily & weekly progress** with visual text progress bars
- 🎯 **Daily nutrition targets** (calories, protein, fat, carbs)
- 💳 **Subscription payments** via Telegram Payments + Click (Uzbek provider)
- 🔔 **Meal reminders** with custom time scheduling
- 🌐 **Bilingual** (Uzbek & Russian)

## Tech Stack

- Spring Boot 3.2.5, Java 17
- PostgreSQL + Flyway migrations
- Redis (scaffolded)
- TelegramBots 6.9.7.1 (long polling)
- WebClient (Spring WebFlux) for Edamam API
- Lombok, Maven

## Project Structure

```
src/main/java/com/mfood/bot/
├── MFoodApplication.java
├── domain/
│   ├── model/          # JPA entities (User, Profile, Meal, MealItem, Subscription, Payment, Reminder)
│   ├── enums/          # Domain enums
│   └── repository/     # Spring Data JPA repositories
├── application/
│   ├── service/        # Business logic services
│   └── dto/            # Data transfer objects
├── infrastructure/
│   ├── edamam/         # Edamam food/vision API client
│   ├── payment/        # Click payment integration
│   ├── config/         # Configuration properties & beans
│   └── scheduler/      # Scheduled tasks (reminders, subscription renewal)
└── presentation/
    ├── bot/            # TelegramLongPollingBot + update router
    ├── handler/        # Per-feature message handlers
    └── keyboard/       # Keyboard markup factories
```

## Setup

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Redis (optional)
- A Telegram bot token (from @BotFather)
- Edamam API credentials (food database)
- Click provider token (for payments)

### Configuration

1. Copy `.env.example` to `.env` and fill in your credentials
2. Create PostgreSQL database: `CREATE DATABASE mfood;`
3. Run the application - Flyway will create all tables automatically

### Environment Variables

| Variable | Description |
|---|---|
| `TELEGRAM_BOT_TOKEN` | Bot token from @BotFather |
| `TELEGRAM_BOT_USERNAME` | Bot username (without @) |
| `EDAMAM_APP_ID` | Edamam Food Database App ID |
| `EDAMAM_APP_KEY` | Edamam Food Database App Key |
| `CLICK_PROVIDER_TOKEN` | Click payment provider token |
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USER` | Database username |
| `DB_PASS` | Database password |
| `REDIS_URL` | Redis URL (optional) |

### Running

```bash
./mvnw spring-boot:run
```

Or with Docker:
```bash
docker run --env-file .env -p 8080:8080 mfood-bot
```

## Nutrition Formula

Uses **Mifflin-St Jeor** formula:
- **Men**: BMR = 10×weight(kg) + 6.25×height(cm) − 5×age + 5
- **Women**: BMR = 10×weight(kg) + 6.25×height(cm) − 5×age − 161
- TDEE = BMR × activity multiplier
- Goal adjustment: −500 kcal (lose), 0 (maintain), +500 kcal (gain)

## Subscription

Monthly subscription: **13,000 UZS/month** via Telegram Payments (Click provider).  
Payments are idempotent - duplicate `telegram_charge_id` values are safely ignored.

## Bot Commands

- `/start` - Start / show language selection
- `/terms` - Terms of service
- `/support` - Support contact info