-- =============================================================
-- Lab 7 - Employee Directory (Java Swing + JDBC + MySQL)
-- Database setup and the CRUD queries used by DatabaseConnector
-- =============================================================

-- MySQL server: Lab 6 Docker container
--   docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -p 3306:3306 -d mysql:latest
-- The application runs the two CREATE statements automatically on start-up,
-- so running this script by hand (e.g. in MySQL Workbench) is optional.

CREATE DATABASE IF NOT EXISTS lab7_db;
USE lab7_db;

CREATE TABLE IF NOT EXISTS employees (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(120)  NOT NULL UNIQUE,
    phone       VARCHAR(20),
    department  VARCHAR(50)   NOT NULL,
    salary      DECIMAL(10,2) NOT NULL,
    join_date   DATE          NOT NULL
);

-- ---------- CRUD statements (as PreparedStatements in Java, ? = parameter) ----------

-- CREATE  (FormJPanel -> DatabaseConnector.addEmployee)
-- INSERT INTO employees (name, email, phone, department, salary, join_date) VALUES (?, ?, ?, ?, ?, ?);

-- READ    (ViewJPanel.populateTable -> DatabaseConnector.getAllEmployees)
SELECT id, name, email, phone, department, salary, join_date
FROM employees
ORDER BY id;

-- UPDATE  (ViewJPanel Edit -> Update -> DatabaseConnector.updateEmployee)
-- UPDATE employees SET name = ?, email = ?, phone = ?, department = ?, salary = ?, join_date = ? WHERE id = ?;

-- DELETE  (ViewJPanel Delete -> DatabaseConnector.deleteEmployee)
-- DELETE FROM employees WHERE id = ?;
