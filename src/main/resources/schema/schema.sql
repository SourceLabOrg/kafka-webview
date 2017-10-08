
create table if not exists `user` (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) unique not null,
  display_name varchar(255) not null,
  password varchar(255) not null,
  role TINYINT NOT NULL DEFAULT 0,
  reset_password_hash VARCHAR(255) default NULL,
  source TINYINT UNSIGNED NOT NULL,
  has_password BOOL NOT NULL DEFAULT FALSE,
  is_active BOOL NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `cluster` (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  broker_hosts TEXT NOT NULL,
  is_valid BOOLEAN DEFAULT FALSE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by INT(11) UNSIGNED DEFAULT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_by INT(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;