CREATE TABLE meals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    meal_date DATE NOT NULL,
    meal_time TIME,
    meal_type VARCHAR(20) DEFAULT 'SNACK',
    total_calories DOUBLE PRECISION DEFAULT 0,
    total_protein DOUBLE PRECISION DEFAULT 0,
    total_fat DOUBLE PRECISION DEFAULT 0,
    total_carbs DOUBLE PRECISION DEFAULT 0,
    photo_file_id VARCHAR(500),
    source VARCHAR(20) DEFAULT 'MANUAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_meals_user_date ON meals(user_id, meal_date);
