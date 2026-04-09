-- Create Database
CREATE DATABASE IF NOT EXISTS ecommerce;

-- Use the database
USE ecommerce;

-- Create users
CREATE USER 'test'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON ecommerce.* TO 'test'@'%';
FLUSH PRIVILEGES;