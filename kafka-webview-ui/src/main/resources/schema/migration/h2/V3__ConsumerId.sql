ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS default_consumer_id TEXT DEFAULT NULL AFTER sasl_config;
ALTER TABLE `view` ADD COLUMN IF NOT EXISTS consumer_id TEXT DEFAULT NULL AFTER results_per_partition;


