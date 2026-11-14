package com.lab7.db;

import com.lab7.model.Employee;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * All JDBC access for the application lives here.
 *
 * Connection settings are read from src/main/resources/db.properties.
 * Every operation opens its own connection with try-with-resources, so
 * connections, statements and result sets are always closed.
 */
public final class DatabaseConnector {

    private static final Properties CONFIG = loadConfig();

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS employees ("
            + " id INT AUTO_INCREMENT PRIMARY KEY,"
            + " name VARCHAR(100) NOT NULL,"
            + " email VARCHAR(120) NOT NULL UNIQUE,"
            + " phone VARCHAR(20),"
            + " department VARCHAR(50) NOT NULL,"
            + " salary DECIMAL(10,2) NOT NULL,"
            + " join_date DATE NOT NULL)";

    private static final String INSERT =
            "INSERT INTO employees (name, email, phone, department, salary, join_date) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL =
            "SELECT id, name, email, phone, department, salary, join_date FROM employees ORDER BY id";
    private static final String UPDATE =
            "UPDATE employees SET name = ?, email = ?, phone = ?, department = ?, salary = ?, join_date = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM employees WHERE id = ?";

    private DatabaseConnector() {
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = DatabaseConnector.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
        return p;
    }

    private static String serverUrl() {
        return "jdbc:mysql://" + CONFIG.getProperty("db.host", "localhost") + ":"
                + CONFIG.getProperty("db.port", "3306") + "/";
    }

    private static String databaseName() {
        return CONFIG.getProperty("db.name", "lab7_db");
    }

    /** Opens a connection to the application's database. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(serverUrl() + databaseName(),
                CONFIG.getProperty("db.user"), CONFIG.getProperty("db.password"));
    }

    /**
     * Creates the database and the EMPLOYEES table if they do not exist yet,
     * so the app works on a fresh MySQL server without running any script first.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(serverUrl(),
                CONFIG.getProperty("db.user"), CONFIG.getProperty("db.password"));
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName());
        }
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_TABLE);
        }
    }

    /** CREATE: inserts a new employee and returns the generated id. */
    public static int addEmployee(Employee e) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** READ: returns every employee ordered by id. */
    public static List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("department"),
                        rs.getBigDecimal("salary"),
                        rs.getDate("join_date").toLocalDate()));
            }
        }
        return list;
    }

    /** UPDATE: saves the edited values for the employee with e.getId(). */
    public static boolean updateEmployee(Employee e) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            bind(ps, e);
            ps.setInt(7, e.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /** DELETE: removes the employee with the given id. */
    public static boolean deleteEmployee(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static void bind(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getName());
        ps.setString(2, e.getEmail());
        ps.setString(3, e.getPhone());
        ps.setString(4, e.getDepartment());
        ps.setBigDecimal(5, e.getSalary());
        ps.setDate(6, Date.valueOf(e.getJoinDate()));
    }

    /** Text shown in the window's status bar. */
    public static String describeTarget() {
        return "MySQL " + CONFIG.getProperty("db.host", "localhost") + ":"
                + CONFIG.getProperty("db.port", "3306") + " / " + databaseName();
    }
}
