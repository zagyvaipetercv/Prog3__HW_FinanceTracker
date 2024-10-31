package financetracker.windowing;

import javax.swing.JOptionPane;

public class ErrorBox {
    private ErrorBox() {
    }

    public static void show(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
