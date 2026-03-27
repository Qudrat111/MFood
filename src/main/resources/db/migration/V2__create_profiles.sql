CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    age INTEGER,
    sex VARCHAR(10),
    height_cm DOUBLE PRECISION,
    weight_kg DOUBLE PRECISION,
    activity_level VARCHAR(20),
    goal VARCHAR(20),
    daily_calorie_target DOUBLE PRECISION,
    daily_protein_target DOUBLE PRECISION,
    daily_fat_target DOUBLE PRECISION,
    daily_carbs_target DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
