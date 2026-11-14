package com.lab7.ui;

import com.lab7.db.DatabaseConnector;
import com.lab7.model.Employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * READ / UPDATE / DELETE view.
 *
 * - The JTable is populated from the database (populateTable).
 * - Selecting a row copies its values into the detail fields (read-only).
 * - "Edit" enables the fields, "Update" validates and writes the changes to MySQL.
 * - "Delete" removes the selected row from MySQL after confirmation.
 */
public class ViewJPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Name", "Email", "Phone", "Department", "Salary", "Join Date"};
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.CANADA);

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // edits happen through the form fields, not in the grid
        }
    };
    private final JTable table = new JTable(tableModel);
    private final List<Employee> rows = new ArrayList<>();

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtName = new JTextField(18);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextField txtPhone = new JTextField(18);
    private final JComboBox<String> cmbDepartment = new JComboBox<>(EmployeeValidator.DEPARTMENTS);
    private final JTextField txtSalary = new JTextField(18);
    private final JTextField txtJoinDate = new JTextField(18);

    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnEdit = new JButton("Edit");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnCancel = new JButton("Cancel");
    private final JButton btnDelete = new JButton("Delete");
    private final JLabel lblCount = new JLabel();

    public ViewJPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("View Employees");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        header.add(title, BorderLayout.WEST);
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.add(lblCount);
        headerRight.add(btnRefresh);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {40, 150, 200, 100, 120, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(800, 230));
        add(scroll, BorderLayout.CENTER);

        add(buildDetails(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        btnRefresh.addActionListener(e -> populateTable());
        btnEdit.addActionListener(e -> setEditing(true));
        btnCancel.addActionListener(e -> {
            setEditing(false);
            showSelected();
        });
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());

        txtId.setEditable(false);
        setEditing(false);
        updateButtons();
    }

    private JPanel buildDetails() {
        JPanel details = new JPanel(new GridBagLayout());
        details.setBorder(BorderFactory.createTitledBorder("Selected Employee"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        place(details, c, 0, 0, "ID:", txtId);
        place(details, c, 0, 2, "Department:", cmbDepartment);
        place(details, c, 1, 0, "Full Name:", txtName);
        place(details, c, 1, 2, "Salary (CAD):", txtSalary);
        place(details, c, 2, 0, "Email:", txtEmail);
        place(details, c, 2, 2, "Join Date:", txtJoinDate);
        place(details, c, 3, 0, "Phone:", txtPhone);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnEdit);
        buttons.add(btnUpdate);
        buttons.add(btnCancel);
        buttons.add(btnDelete);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.insets = new Insets(12, 8, 4, 8);
        details.add(buttons, c);
        return details;
    }

    private static void place(JPanel p, GridBagConstraints c, int row, int col, String label, java.awt.Component f) {
        c.gridwidth = 1;
        c.gridy = row;
        c.gridx = col;
        c.weightx = 0;
        p.add(new JLabel(label), c);
        c.gridx = col + 1;
        c.weightx = 1;
        p.add(f, c);
    }

    /** READ: reloads every employee from MySQL into the JTable. */
    public void populateTable() {
        populateTable(-1);
    }

    private void populateTable(int selectId) {
        try {
            List<Employee> fresh = DatabaseConnector.getAllEmployees();
            rows.clear();
            rows.addAll(fresh);
            tableModel.setRowCount(0);
            for (Employee e : rows) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getName(), e.getEmail(), e.getPhone(),
                    e.getDepartment(), MONEY.format(e.getSalary()), e.getJoinDate().toString()
                });
            }
            lblCount.setText(rows.size() + (rows.size() == 1 ? " record" : " records"));
            setEditing(false);
            table.clearSelection();
            if (selectId > 0) {
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getId() == selectId) {
                        table.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
            showSelected();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load employees from the database:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Employee selected() {
        int r = table.getSelectedRow();
        return r < 0 ? null : rows.get(table.convertRowIndexToModel(r));
    }

    private void showSelected() {
        Employee e = selected();
        if (e == null) {
            txtId.setText("");
            txtName.setText("");
            txtEmail.setText("");
            txtPhone.setText("");
            cmbDepartment.setSelectedIndex(0);
            txtSalary.setText("");
            txtJoinDate.setText("");
        } else {
            txtId.setText(String.valueOf(e.getId()));
            txtName.setText(e.getName());
            txtEmail.setText(e.getEmail());
            txtPhone.setText(e.getPhone());
            cmbDepartment.setSelectedItem(e.getDepartment());
            txtSalary.setText(e.getSalary().toPlainString());
            txtJoinDate.setText(e.getJoinDate().toString());
        }
        updateButtons();
    }

    private boolean editing;

    private void setEditing(boolean on) {
        editing = on;
        txtName.setEnabled(on);
        txtEmail.setEnabled(on);
        txtPhone.setEnabled(on);
        cmbDepartment.setEnabled(on);
        txtSalary.setEnabled(on);
        txtJoinDate.setEnabled(on);
        table.setEnabled(!on);
        btnRefresh.setEnabled(!on);
        updateButtons();
        if (on) {
            txtName.requestFocusInWindow();
        }
    }

    private void updateButtons() {
        boolean has = selected() != null;
        btnEdit.setEnabled(has && !editing);
        btnDelete.setEnabled(has && !editing);
        btnUpdate.setEnabled(has && editing);
        btnCancel.setEnabled(has && editing);
    }

    /** UPDATE: validates the edited fields and saves them to MySQL. */
    private void update() {
        Employee current = selected();
        if (current == null) {
            return;
        }
        Employee edited;
        try {
            edited = EmployeeValidator.build(txtName.getText(), txtEmail.getText(), txtPhone.getText(),
                    cmbDepartment.getSelectedItem(), txtSalary.getText(), txtJoinDate.getText());
        } catch (EmployeeValidator.ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        edited.setId(current.getId());
        try {
            if (DatabaseConnector.updateEmployee(edited)) {
                JOptionPane.showMessageDialog(this, "Employee ID " + edited.getId() + " updated in the database.",
                        "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            populateTable(edited.getId());
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Another employee already uses this email.",
                    "Duplicate email", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not update the database:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** DELETE: removes the selected employee from MySQL after confirmation. */
    private void delete() {
        Employee e = selected();
        if (e == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete employee \"" + e.getName() + "\" (ID " + e.getId() + ") from the database?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            DatabaseConnector.deleteEmployee(e.getId());
            populateTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete from the database:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
