package com.lab7.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Plain model object for one row of the EMPLOYEES table.
 */
public class Employee {

    private int id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private BigDecimal salary;
    private LocalDate joinDate;

    public Employee() {
    }

    public Employee(int id, String name, String email, String phone,
                    String department, BigDecimal salary, LocalDate joinDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.salary = salary;
        this.joinDate = joinDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    @Override
    public String toString() {
        return name;
    }
}
