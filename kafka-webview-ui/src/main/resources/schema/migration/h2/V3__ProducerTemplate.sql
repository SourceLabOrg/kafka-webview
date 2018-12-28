CREATE TABLE IF NOT EXISTS serializer_format
(
  id                INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name              VARCHAR(255) UNIQUE NOT NULL,
  classpath         TEXT                NOT NULL,
  jar               TEXT                NOT NULL,
  option_parameters TEXT                NOT NULL DEFAULT '{}',
  is_default        BOOLEAN                      DEFAULT FALSE NOT NULL,
  created_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by        INT(11) UNSIGNED DEFAULT NULL,
  updated_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by        INT(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS partitioning_strategy
(
  id                INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name              VARCHAR(255) UNIQUE NOT NULL,
  classpath         TEXT                NOT NULL,
  jar               TEXT                NOT NULL,
  option_parameters TEXT                NOT NULL DEFAULT '{}',
  is_default        BOOLEAN                      DEFAULT FALSE NOT NULL,
  created_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by        INT(11) UNSIGNED DEFAULT NULL,
  updated_at        TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by        INT(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS producer_template (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  cluster_id INT(11) UNSIGNED NOT NULL,
  topic TEXT NOT NULL,
  key_serializer_format_id INT(11) UNSIGNED NOT NULL,
  value_serializer_format_id INT(11) UNSIGNED NOT NULL,
  partitioning_strategy_id INT(11) UNSIGNED NOT NULL,
  option TEXT NOT NULL DEFAULT '{}',
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by INT(11) UNSIGNED DEFAULT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_by INT(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (cluster_id) REFERENCES cluster(id),
  FOREIGN KEY (key_serializer_format_id) REFERENCES serializer_format(id),
  FOREIGN KEY (value_serializer_format_id) REFERENCES serializer_format(id),
  FOREIGN KEY (partitioning_strategy_id) REFERENCES partitioning_strategy(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;