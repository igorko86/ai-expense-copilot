CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Додаємо зв'язок витрат із користувачем.
-- Поки що робимо NULLABLE, щоб не впасти, якщо в БД вже є старі тестові витрати.
ALTER TABLE expenses
ADD COLUMN user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
