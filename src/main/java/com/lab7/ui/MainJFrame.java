package com.lab7.ui;

import com.lab7.db.DatabaseConnector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Main window: a control panel on the left switches between the two views
 * (Create form and View table) shown in the work area on the right.
 */
public class MainJFrame extends JFrame {

    private static final String CREATE = "create";
    private static final String VIEW = "view";

    private final CardLayout cards = new CardLayout();
    private final JPanel workArea = new JPanel(cards);
    private final ViewJPanel viewPanel = new ViewJPanel();
    private final JLabel status = new JLabel();

    public MainJFrame(boolean connected, String connectionMessage) {
        super("Lab 7 – Employee Directory (Java Swing + JDBC + MySQL)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FormJPanel formPanel = new FormJPanel(() -> {
            viewPanel.populateTable();
            setStatus("Saved. " + DatabaseConnector.describeTarget(), true);
        });
        workArea.add(formPanel, CREATE);
        workArea.add(viewPanel, VIEW);

        JPanel control = new JPanel(new BorderLayout());
        control.setBackground(new Color(31, 58, 95));
        control.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        JLabel appName = new JLabel("<html>Employee<br>Directory</html>");
        appName.setForeground(Color.WHITE);
        appName.setFont(appName.getFont().deriveFont(Font.BOLD, 18f));
        appName.setBorder(BorderFactory.createEmptyBorder(0, 4, 24, 0));
        control.add(appName, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 10));
        nav.setOpaque(false);
        JButton btnCreate = new JButton("Create Employee");
        JButton btnView = new JButton("View Employees");
        nav.add(btnCreate);
        nav.add(btnView);
        JPanel navHolder = new JPanel(new BorderLayout());
        navHolder.setOpaque(false);
        navHolder.add(nav, BorderLayout.NORTH);
        control.add(navHolder, BorderLayout.CENTER);
        control.setPreferredSize(new Dimension(190, 0));

        btnCreate.addActionListener(e -> cards.show(workArea, CREATE));
        btnView.addActionListener(e -> {
            viewPanel.populateTable();
            cards.show(workArea, VIEW);
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, control, workArea);
        split.setDividerSize(4);
        split.setEnabled(false);

        status.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        status.setOpaque(true);
        setStatus(connectionMessage, connected);

        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(status, BorderLayout.SOUTH);
        setSize(1060, 640);
        setLocationRelativeTo(null);
    }

    private void setStatus(String text, boolean ok) {
        status.setText((ok ? "● Connected: " : "● Not connected: ") + text);
        status.setForeground(ok ? new Color(21, 101, 52) : new Color(160, 30, 30));
        status.setBackground(ok ? new Color(232, 245, 236) : new Color(253, 236, 236));
    }
}
