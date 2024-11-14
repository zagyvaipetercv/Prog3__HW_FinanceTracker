package financetracker.windowing;

import javax.swing.JOptionPane;

import financetracker.exceptions.ErrorBoxException;

public class ErrorBox {
    private ErrorBox() {
    }

    public static void show(ErrorBoxException e) {
        show(e.getErrorTitle(), e.getMessage());
    }

    public static void show(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
