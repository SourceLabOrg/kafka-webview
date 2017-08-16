
INSERT INTO `user` (`email`, `display_name`, `password`, `is_active`, `role`, `source`, `has_password`) VALUES
  ('stephen.powis@gmail.com', 'StevieP', '$2a$10$NaWlT1v3dfidu4FMC4Xnw.7HEfq6mFhvVuzo9..CcOOmy8OZDt8A2', 1, 1, 0, true),
  ('user@gmail.com', 'User', '$2a$10$NaWlT1v3dfidu4FMC4Xnw.7HEfq6mFhvVuzo9..CcOOmy8OZDt8A2', 1, 0, 0, true);

insert into `location` (user_id, lat, lng, created_at) VALUES
  (1, 35.689487, 139.691706, now()),
  (2, -89.689487, 139.691706, now());