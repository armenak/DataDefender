create table log_table_name (
  id int(11) AUTO_INCREMENT PRIMARY KEY,
  eventDate TIMESTAMP,
  level VARCHAR(20),
  logger VARCHAR(100),
  message TEXT,
  exception TEXT
);
