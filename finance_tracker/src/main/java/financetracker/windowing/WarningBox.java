package financetracker.windowing;

import java.awt.Component;

import javax.swing.JOptionPane;

import financetracker.exceptions.WarningBoxException;

public class WarningBox {
    private WarningBox() {
    }

    /**
     * Opens a Warning message JOptionPane with the set title and message
     * 
     * @param parentComponent parnet component of the option pane
     * @param title           title of the option pane
     * @param message         message of the option pane
     */
    public static void show(Component parent, WarningBoxException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), e.getErrorTitle(), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Opens a Warning message JOptionPane with the set title and message
     * 
     * @param parentComponent parnet component of the option pane
     * @param e               exceptions title and message will be the title and
     *                        message for the option pane
     */
    public static void show(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
}
