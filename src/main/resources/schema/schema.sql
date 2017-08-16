
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

create table if not exists `location` (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id INT(11) UNSIGNED NOT NULL,
  lat DECIMAL(10, 8) NOT NULL,
  lng DECIMAL(11, 8) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY fk_user_id(user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
