create database flowers;
use flowers;
create table flowers_metadata (id INT AUTO_INCREMENT PRIMARY KEY, text VARCHAR(255) NOT NULL, label INT NOT NULL, fileName VARCHAR(255) NOT NULL);