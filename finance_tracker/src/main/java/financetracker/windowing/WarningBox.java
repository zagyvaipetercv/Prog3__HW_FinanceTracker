package financetracker.windowing;

import java.awt.Component;

import javax.swing.JOptionPane;

import financetracker.exceptions.WarningBoxException;

public class WarningBox {
    private WarningBox() {
    }

    public static void show(Component parent, WarningBoxException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), e.getErrorTitle(), JOptionPane.WARNING_MESSAGE);
    }

    public static void show(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
}
