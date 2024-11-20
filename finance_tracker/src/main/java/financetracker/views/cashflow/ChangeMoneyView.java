package financetracker.views.cashflow;

import java.time.LocalDate;
import java.util.Currency;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.CashFlowController;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

public class ChangeMoneyView extends FrameView {
    public ChangeMoneyView(CashFlowController cashFlowController) {
        // Setup Window
        setTitle("Finance Tracker - Change Money");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        // Define Components
        JLabel dateLabel = new JLabel("Select Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setDate(LocalDate.now());

        JLabel amountLabel = new JLabel("Amount:");
        JLabel currencyLabel = new JLabel("Currency:");
        JLabel reasonLabel = new JLabel("Reason:");

        JTextField amountTextField = new JTextField(10);
        JLabel hufLabel = new JLabel("HUF");
        JTextField reasonTextField = new JTextField(10);
        JButton submitButton = new JButton("Submit");

        // Add button action listener
        submitButton.addActionListener(ae -> {
            LocalDate date = datePicker.getDate();
            String amount = amountTextField.getText();
            Currency currency = Currency.getInstance("HUF");
            String reason = reasonTextField.getText();
            try {
                cashFlowController.changeMoneyOnAccount(date, amount, currency, reason);
                cashFlowController.refreshWalletView();
                cashFlowController.closeFrameView(this);
            } catch (InvalidAmountException | InvalidReasonException | BalanceCouldNotCahcngeException e) {
                ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
            } catch (Exception e) {
                ErrorBox.show(this, "ERROR", e.getMessage());
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
            .addComponent(amountTextField)
            .addComponent(dateLabel));

        hGroup.addGroup(layout.createParallelGroup()
            .addComponent(currencyLabel)
            .addComponent(hufLabel)
            .addComponent(datePicker));

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

        vGroup.addGroup(layout.createParallelGroup()
            .addComponent(dateLabel)
            .addComponent(datePicker));

        

        layout.setVerticalGroup(vGroup);

        add(panel);
        
        // Show panel
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        setVisible(true);
    }
}
