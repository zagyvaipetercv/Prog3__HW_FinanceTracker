package financetracker.windowing;

import java.awt.Component;

import javax.swing.JOptionPane;

import financetracker.exceptions.ErrorBoxException;

/**
 * Static class made for showing Error messages
 */
public class ErrorBox {
    private ErrorBox() {
    }

    /**
     * Opens an Error message JOptionPane with the set title and message
     * 
     * @param parentComponent parnet component of the option pane
     * @param title           title of the option pane
     * @param message         message of the option pane
     */
    public static void show(Component parentComponent, String title, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Opens an Error message JOptionPane with the set title and message
     * 
     * @param parentComponent parnet component of the option pane
     * @param e               exceptions title and message will be the title and
     *                        message for the option pane
     */
    public static void show(Component parentComponent, ErrorBoxException e) {
        JOptionPane.showMessageDialog(parentComponent, e.getMessage(), e.getErrorTitle(), JOptionPane.ERROR_MESSAGE);
    }
}
