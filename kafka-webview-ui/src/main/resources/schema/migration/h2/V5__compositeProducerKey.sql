ALTER TABLE `producer` DROP CONSTRAINT `constraint_f2`;

ALTER TABLE `producer` ADD CONSTRAINT `unique_producer_topic` UNIQUE(`topic`,`cluster_id`);