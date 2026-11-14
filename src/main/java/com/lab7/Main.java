package com.lab7;

import com.lab7.db.DatabaseConnector;
import com.lab7.ui.MainJFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Entry point. Creates the database/table if needed, then opens the main window.
 */
public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }

        boolean connected;
        String message;
        try {
            DatabaseConnector.initializeDatabase();
            connected = true;
            message = DatabaseConnector.describeTarget();
        } catch (SQLException ex) {
            connected = false;
            message = ex.getMessage();
        }

        final boolean ok = connected;
        final String msg = message;
        SwingUtilities.invokeLater(() -> {
            MainJFrame frame = new MainJFrame(ok, msg);
            frame.setVisible(true);
            if (!ok) {
                JOptionPane.showMessageDialog(frame,
                        "Could not connect to MySQL (" + DatabaseConnector.describeTarget() + ").\n"
                        + "Make sure the MySQL server is running and db.properties is correct.\n\n" + msg,
                        "Database connection failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
