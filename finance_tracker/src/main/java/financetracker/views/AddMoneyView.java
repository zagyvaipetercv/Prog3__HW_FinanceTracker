package financetracker.views;

import java.util.Currency;
import java.util.Locale;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import financetracker.controllers.MoneyController;
import financetracker.exceptions.moneycontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.moneycontroller.MoneyAmountIsInvalidException;
import financetracker.exceptions.moneycontroller.ReasonIsInvalidException;
import financetracker.windowing.ErrorBox;

public class AddMoneyView extends FrameView {

    public AddMoneyView(MoneyController moneyController) {
        // Setup Window
        setTitle("Finance Tracker - Add Money");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        // Define Components
        JLabel amountLabel = new JLabel("Amount:");
        JLabel currencyLabel = new JLabel("Currency:");
        JLabel reasonLabel = new JLabel("Reason:");

        JTextField amountTextField = new JTextField(10);
        JLabel hufLabel = new JLabel("HUF");
        JTextField reasonTextField = new JTextField(10);
        JButton submitButton = new JButton("Submit");

        // Add button action listener
        submitButton.addActionListener(ae -> {
            double amount = Double.parseDouble(amountTextField.getText());
            Currency currency = Currency.getInstance("HUF");
            String reason = reasonTextField.getText();
            try {
                moneyController.addMoneyToAccount(amount, currency, reason);
            } catch (MoneyAmountIsInvalidException | ReasonIsInvalidException | BalanceCouldNotCahcngeException e) {
                ErrorBox.show(e.getErrorTitle(), e.getMessage());
            } catch (Exception e) {
                ErrorBox.show("ERROR", e.getMessage());
            }
        });

        // Setup Layout
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Add components to layout
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup()
            .addComponent(amountLabel)
            .addComponent(amountTextField));

        hGroup.addGroup(layout.createParallelGroup()
            .addComponent(currencyLabel)
            .addComponent(hufLabel));

        hGroup.addGroup(layout.createParallelGroup()
            .addComponent(reasonLabel)
            .addComponent(reasonTextField));

        hGroup.addGroup(layout.createParallelGroup()
            .addComponent(submitButton));

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup()
            .addComponent(amountLabel)
            .addComponent(currencyLabel)
            .addComponent(reasonLabel));

        vGroup.addGroup(layout.createParallelGroup()
            .addComponent(amountTextField)
            .addComponent(hufLabel)
            .addComponent(reasonTextField)
            .addComponent(submitButton));

        layout.setVerticalGroup(vGroup);

        add(panel);
        
        // Show panel
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        setVisible(true);
    }
}
