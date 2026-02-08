CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    month DATE NOT NULL,
    budget_type VARCHAR(10) NOT NULL CHECK (budget_type IN ('OVERALL', 'CATEGORY')),
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE SET NULL,

    CONSTRAINT unique_user_budget_per_scope
        UNIQUE (user_id, month, category_id),

    CONSTRAINT check_category_null_for_overall
        CHECK (
            (budget_type = 'OVERALL' AND category_id IS NULL) OR
            (budget_type = 'CATEGORY' AND category_id IS NOT NULL)
        )
);

COMMENT ON COLUMN budgets.month IS 'Stores first day of month (e.g., 2024-12-01)';
COMMENT ON COLUMN budgets.budget_type IS 'OVERALL for total budget, CATEGORY for category-specific';