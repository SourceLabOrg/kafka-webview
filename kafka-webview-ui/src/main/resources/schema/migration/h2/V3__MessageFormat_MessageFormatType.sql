-- Add new field message_format_type
-- Values 0 = Default, 1 = Custom, 2 = AutoCreated
ALTER TABLE `message_format` ADD COLUMN IF NOT EXISTS message_format_type INT(1) DEFAULT 1 NOT NULL AFTER is_default_format;

-- Populate new field based on message_format.is_default_format values.
UPDATE `message_format` SET message_format_type=1 WHERE is_default_format = false;
UPDATE `message_format` SET message_format_type=0 WHERE is_default_format = true;

-- Drop now dead field is_default_format
ALTER TABLE `message_format` DROP COLUMN is_default_format;