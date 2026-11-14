package com.lab7.ui;

import com.lab7.db.DatabaseConnector;
import com.lab7.model.Employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;

/**
 * CREATE view: a form that validates input and inserts a new employee into MySQL.
 */
public class FormJPanel extends JPanel {

    private final JTextField txtName = new JTextField(22);
    private final JTextField txtEmail = new JTextField(22);
    private final JTextField txtPhone = new JTextField(22);
    private final JComboBox<String> cmbDepartment = new JComboBox<>(EmployeeValidator.DEPARTMENTS);
    private final JTextField txtSalary = new JTextField(22);
    private final JTextField txtJoinDate = new JTextField(22);
    private final JButton btnSave = new JButton("Save Employee");
    private final JButton btnClear = new JButton("Clear");
    private final Runnable onSaved;

    public FormJPanel(Runnable onSaved) {
        this.onSaved = onSaved;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("Create Employee");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;

        addRow(form, c, 0, "Full Name:", txtName);
        addRow(form, c, 1, "Email:", txtEmail);
        addRow(form, c, 2, "Phone (10 digits):", txtPhone);
        addRow(form, c, 3, "Department:", cmbDepartment);
        addRow(form, c, 4, "Salary (CAD):", txtSalary);
        addRow(form, c, 5, "Join Date (YYYY-MM-DD):", txtJoinDate);

        JPanel buttons = new JPanel();
        buttons.add(btnSave);
        buttons.add(btnClear);
        c.gridx = 1;
        c.gridy = 6;
        c.insets = new Insets(18, 0, 0, 0);
        form.add(buttons, c);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);

        txtJoinDate.setText(LocalDate.now().toString());
        btnSave.addActionListener(e -> save());
        btnClear.addActionListener(e -> clear());
    }

    private static void addRow(JPanel form, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridx = 0;
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
    }

    private void save() {
        Employee emp;
        try {
            emp = EmployeeValidator.build(txtName.getText(), txtEmail.getText(), txtPhone.getText(),
                    cmbDepartment.getSelectedItem(), txtSalary.getText(), txtJoinDate.getText());
        } catch (EmployeeValidator.ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = DatabaseConnector.addEmployee(emp);
            JOptionPane.showMessageDialog(this,
                    "Employee \"" + emp.getName() + "\" saved to the database with ID " + id + ".",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            clear();
            onSaved.run();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "An employee with this email already exists.",
                    "Duplicate email", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save to the database:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clear() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        cmbDepartment.setSelectedIndex(0);
        txtSalary.setText("");
        txtJoinDate.setText(LocalDate.now().toString());
        txtName.requestFocusInWindow();
    }
}
