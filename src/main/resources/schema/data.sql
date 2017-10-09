
INSERT INTO `user` (`email`, `display_name`, `password`, `is_active`, `role`, `source`, `has_password`) VALUES
  ('stephen.powis@gmail.com', 'StevieP', '$2a$10$NaWlT1v3dfidu4FMC4Xnw.7HEfq6mFhvVuzo9..CcOOmy8OZDt8A2', 1, 1, 0, true),
  ('user@gmail.com', 'User', '$2a$10$NaWlT1v3dfidu4FMC4Xnw.7HEfq6mFhvVuzo9..CcOOmy8OZDt8A2', 1, 0, 0, true);

INSERT INTO `cluster` (`name`, `broker_hosts`, `is_valid`) VALUES
  ('Localhost Cluster', 'localhost:9092', true);

INSERT INTO `message_format` (`name`, `classpath`, `jar`, `is_default_format`) VALUES
  ('Short', 'org.apache.kafka.common.serialization.ShortDeserializer', 'n/a', true),
  ('ByteArray', 'org.apache.kafka.common.serialization.ByteArrayDeserializer', 'n/a', true),
  ('Bytes', 'org.apache.kafka.common.serialization.BytesDeserializer', 'n/a', true),
  ('Double', 'org.apache.kafka.common.serialization.DoubleDeserializer', 'n/a', true),
  ('Float', 'org.apache.kafka.common.serialization.FloatDeserializer', 'n/a', true),
  ('Integer', 'org.apache.kafka.common.serialization.IntegerDeserializer', 'n/a', true),
  ('Long', 'org.apache.kafka.common.serialization.LongDeserializer', 'n/a', true),
  ('String', 'org.apache.kafka.common.serialization.StringDeserializer', 'n/a', true);
