/*
TODO update producer table with the following when we want to send more than a map of string/string as a kafka message
   key_message_format_id INT(11) UNSIGNED NOT NULL,
   value_message_format_id INT(11) UNSIGNED NOT NULL,
   FOREIGN KEY (key_message_format_id) REFERENCES message_format(id),
   FOREIGN KEY (value_message_format_id) REFERENCES message_format(id)
*/
CREATE TABLE IF NOT EXISTS `producer` (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  cluster_id INT(11) UNSIGNED NOT NULL,
  topic VARCHAR(150) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by INT(11) UNSIGNED DEFAULT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_by INT(11) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (cluster_id) REFERENCES cluster(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `producer_message` (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  qualified_class_name TEXT NOT NULL,
  producer_id INT(11) UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by INT(11) UNSIGNED DEFAULT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_by INT(11) UNSIGNED DEFAULT NULL,
  property_name_list TEXT NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (producer_id) REFERENCES producer(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

