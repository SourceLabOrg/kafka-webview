ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS is_sasl_enabled BOOLEAN DEFAULT FALSE NOT NULL AFTER key_store_password;
ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS sasl_mechanism TEXT DEFAULT NULL AFTER is_sasl_enabled;
ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS sasl_config TEXT DEFAULT '{}' AFTER sasl_mechanism;


