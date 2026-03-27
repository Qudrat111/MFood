CREATE TABLE meal_items (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    food_name VARCHAR(500) NOT NULL,
    quantity DOUBLE PRECISION DEFAULT 100,
    unit VARCHAR(50) DEFAULT 'g',
    calories DOUBLE PRECISION DEFAULT 0,
    protein DOUBLE PRECISION DEFAULT 0,
    fat DOUBLE PRECISION DEFAULT 0,
    carbs DOUBLE PRECISION DEFAULT 0,
    edamam_food_id VARCHAR(255)
);
CREATE INDEX idx_meal_items_meal_id ON meal_items(meal_id);
