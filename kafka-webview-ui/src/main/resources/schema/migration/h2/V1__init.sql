
create table if not exists `user` (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) unique not null,
    display_name varchar(255) not null,
    password varchar(255) not null,
    role TINYINT NOT NULL DEFAULT 0,
    reset_password_hash VARCHAR(255) default NULL,
    has_password BOOL NOT NULL DEFAULT FALSE,
    is_active BOOL NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS `cluster` (
   id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR(255) UNIQUE NOT NULL,
   broker_hosts TEXT NOT NULL,
   is_ssl_enabled BOOLEAN DEFAULT FALSE NOT NULL,
   trust_store_file TEXT DEFAULT NULL,
   trust_store_password TEXT DEFAULT NULL,
   key_store_file TEXT DEFAULT NULL,
   key_store_password TEXT DEFAULT NULL,
   is_valid BOOLEAN DEFAULT FALSE NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
   created_by INT DEFAULT NULL,
   updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
   updated_by INT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `message_format` (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    classpath TEXT NOT NULL,
    jar TEXT NOT NULL,
    option_parameters TEXT NOT NULL DEFAULT '{}',
    is_default_format BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INT DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by INT DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `view` (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    cluster_id INT NOT NULL,
    key_message_format_id INT NOT NULL,
    value_message_format_id INT NOT NULL,
    topic TEXT NOT NULL,
    partitions TEXT NOT NULL,
    results_per_partition INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INT DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by INT DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (cluster_id) REFERENCES cluster(id),
    FOREIGN KEY (key_message_format_id) REFERENCES message_format(id),
    FOREIGN KEY (value_message_format_id) REFERENCES message_format(id)
);

CREATE TABLE IF NOT EXISTS `filter` (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    classpath TEXT NOT NULL,
    jar TEXT NOT NULL,
    options TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INT DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by INT DEFAULT NULL,
    PRIMARY KEY (id)
);

-- Defines which Filters are enforced on which Views
CREATE TABLE IF NOT EXISTS `view_to_filter_enforced` (
    id INT NOT NULL AUTO_INCREMENT,
    filter_id INT NOT NULL,
    view_id INT NOT NULL,
    option_parameters TEXT NOT NULL DEFAULT '{}',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INT DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by INT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (filter_id, view_id),
    FOREIGN KEY (filter_id) REFERENCES filter(id),
    FOREIGN KEY (view_id) REFERENCES view(id)
);

-- Defines which Filters are available for which Views
CREATE TABLE IF NOT EXISTS `view_to_filter_optional` (
    id INT NOT NULL AUTO_INCREMENT,
    filter_id INT NOT NULL,
    view_id INT NOT NULL,
    option_parameters TEXT NOT NULL DEFAULT '{}',
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INT DEFAULT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by INT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (filter_id, view_id),
    FOREIGN KEY (filter_id) REFERENCES filter(id),
    FOREIGN KEY (view_id) REFERENCES view(id)
);
