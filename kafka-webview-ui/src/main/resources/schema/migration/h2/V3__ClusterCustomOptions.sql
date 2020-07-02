ALTER TABLE `cluster` ADD COLUMN IF NOT EXISTS option_parameters TEXT NOT NULL DEFAULT '{}' AFTER broker_hosts;
UPDATE `cluster` set option_parameters = '{}' where option_parameters IS NULL;


