package financetracker.windowing;

import java.awt.Component;

import javax.swing.JOptionPane;

import financetracker.exceptions.ErrorBoxException;

public class ErrorBox {
    private ErrorBox() {
    }

    public static void show(Component parentComponent, String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void show(Component parentComponent, ErrorBoxException e) {
        JOptionPane.showMessageDialog(parentComponent, e.getMessage(), e.getErrorTitle(), JOptionPane.ERROR_MESSAGE);
    }
}
