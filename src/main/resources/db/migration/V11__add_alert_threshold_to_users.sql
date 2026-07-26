ALTER TABLE users

ADD COLUMN IF NOT EXISTS alert_threshold INTEGER DEFAULT 80;

UPDATE users SET alert_threshold = 80 WHERE alert_threshold IS NULL;