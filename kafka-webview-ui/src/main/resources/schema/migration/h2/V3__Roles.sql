-- Create role table
CREATE TABLE IF NOT EXISTS `role`
(
  id                   INT(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
  name                 VARCHAR(255) UNIQUE NOT NULL,
  created_at           TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by           INT(11) UNSIGNED             DEFAULT NULL,
  updated_at           TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by           INT(11) UNSIGNED             DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;

-- Create role_permission table
CREATE TABLE IF NOT EXISTS `role_permission`
(
  id                   INT(11) UNSIGNED    NOT NULL AUTO_INCREMENT,
  role_id              INT(11) UNSIGNED NOT NULL,
  permission           VARCHAR(255) NOT NULL,
  created_at           TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by           INT(11) UNSIGNED             DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (role_id) REFERENCES role(id),
  UNIQUE KEY (role_id, permission)
) DEFAULT CHARSET=utf8;


ALTER TABLE `user` ADD COLUMN IF NOT EXISTS role_id INT(11) UNSIGNED DEFAULT NULL AFTER role;

-- Add default user roles.
INSERT INTO `role` (id, name) values (1, 'Default Administrator'), (2, 'Default User');

-- Add default admin role_permissions (Placeholder?)
INSERT INTO `role_permission` (role_id, permission) values
  (1, 'CLUSTER_READ'),
  (1, 'CLUSTER_CREATE'),
  (1, 'CLUSTER_MODIFY'),
  (1, 'CLUSTER_DELETE'),

  (1, 'VIEW_READ'),
  (1, 'VIEW_CREATE'),
  (1, 'VIEW_MODIFY'),
  (1, 'VIEW_DELETE'),

  (1, 'TOPIC_READ'),
  (1, 'TOPIC_CREATE'),
  (1, 'TOPIC_MODIFY'),
  (1, 'TOPIC_DELETE'),

  (1, 'CONSUMER_READ'),
  (1, 'CONSUMER_CREATE'),
  (1, 'CONSUMER_MODIFY'),
  (1, 'CONSUMER_DELETE'),

  (1, 'USER_READ'),
  (1, 'USER_CREATE'),
  (1, 'USER_MODIFY'),
  (1, 'USER_DELETE');

-- Add default user role_permissions
INSERT INTO `role_permission` (role_id, permission) values
  (2, 'CLUSTER_READ'),
  (2, 'VIEW_READ'),
  (2, 'TOPIC_READ'),
  (2, 'CONSUMER_READ'),
  (2, 'USER_READ');

-- Migrate all admin users to correct role.
UPDATE `user` set role_id = 1 where role = 1;

-- Migrate all non-admin users to correct role.
UPDATE `user` set role_id = 2 where role = 2;

-- Drop role column
ALTER TABLE `user` DROP COLUMN role;

-- Adjust role_id field to be non-null and a FK to role.id field.
ALTER TABLE `user` MODIFY COLUMN `role_id` INT(11) UNSIGNED DEFAULT NOT NULL;
ALTER TABLE `user` ADD FOREIGN KEY (role_id) REFERENCES role(id);