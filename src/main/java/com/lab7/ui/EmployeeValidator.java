package com.lab7.ui;

import com.lab7.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validates the text typed into the form fields and turns it into an Employee.
 * Used by both the Create form and the View/Edit panel so the rules are identical.
 */
final class EmployeeValidator {

    static final String[] DEPARTMENTS = {
        "Engineering", "Finance", "Human Resources", "Marketing", "Operations", "Sales"
    };

    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,99}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\d{10}$");

    private EmployeeValidator() {
    }

    /** Thrown with a user-friendly message when a field is invalid. */
    static final class ValidationException extends Exception {
        ValidationException(String message) {
            super(message);
        }
    }

    static Employee build(String name, String email, String phone, Object department,
                          String salary, String joinDate) throws ValidationException {
        name = name.trim();
        email = email.trim();
        phone = phone.trim();
        salary = salary.trim();
        joinDate = joinDate.trim();

        if (!NAME.matcher(name).matches()) {
            throw new ValidationException("Name must start with a letter and contain only letters, spaces, . ' or -.");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Please enter a valid email address (e.g. name@company.com).");
        }
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
        if (department == null || department.toString().isBlank()) {
            throw new ValidationException("Please choose a department.");
        }
        BigDecimal pay;
        try {
            pay = new BigDecimal(salary);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Salary must be a number (e.g. 72000 or 72000.50).");
        }
        if (pay.signum() <= 0 || pay.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("Salary must be greater than 0 and less than 100,000,000.");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(joinDate);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Join date must be in the format YYYY-MM-DD.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Join date cannot be in the future.");
        }
        return new Employee(0, name, email, phone, department.toString(),
                pay.setScale(2, java.math.RoundingMode.HALF_UP), date);
    }
}
