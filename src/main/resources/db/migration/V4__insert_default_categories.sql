-- System default categories (user_id will be NULL for system defaults)
INSERT INTO categories (name, type, description, color, icon) VALUES
-- Income Categories
('Salary', 'INCOME', 'Monthly salary income', '#28a745', 'dollar'),
('Freelance', 'INCOME', 'Freelance work income', '#20c997', 'briefcase'),
('Investment', 'INCOME', 'Investment returns', '#17a2b8', 'trending-up'),
('Gift', 'INCOME', 'Gifts received', '#6f42c1', 'gift'),
('Other Income', 'INCOME', 'Other income sources', '#6c757d', 'more-horizontal'),

-- Expense Categories
('Food & Dining', 'EXPENSE', 'Groceries, restaurants, etc.', '#dc3545', 'utensils'),
('Transportation', 'EXPENSE', 'Fuel, public transport, taxi', '#fd7e14', 'car'),
('Shopping', 'EXPENSE', 'Clothes, electronics, etc.', '#e83e8c', 'shopping-bag'),
('Entertainment', 'EXPENSE', 'Movies, games, hobbies', '#6f42c1', 'film'),
('Bills & Utilities', 'EXPENSE', 'Electricity, water, internet', '#007bff', 'file-text'),
('Healthcare', 'EXPENSE', 'Medicine, doctor visits', '#20c997', 'heart'),
('Education', 'EXPENSE', 'Courses, books, tuition', '#17a2b8', 'book'),
('Travel', 'EXPENSE', 'Flights, hotels, vacations', '#28a745', 'globe'),
('Other Expense', 'EXPENSE', 'Other expenses', '#6c757d', 'more-horizontal');