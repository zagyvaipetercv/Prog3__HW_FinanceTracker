package financetracker.views.debt;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.Currency;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.DebtController;
import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.debtcontroller.DeptPaymentFailedException;
import financetracker.exceptions.debtcontroller.PaymentIsGreaterThanRemaining;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.WarningBox;

/**
 * FrameView where the user can add payments to a sleected debt
 */
public class AddPaymentView extends FrameView {
        private DebtController debtController;
        private Debt debt;

        private JButton repayButton;
        private JButton repayAllButton;
        private JButton cancelButton;

        private JTextField amountTextField;
        private DatePicker datePicker;

        public AddPaymentView(DebtController debtController, Debt debt) {
                this.debtController = debtController;
                this.debt = debt;

                setTitle("Add Payment");

                setLayout(new BorderLayout());

                initComponents();

                repayButton.addActionListener(ae -> {
                        try {
                                debtController.payDebt(debt, amountTextField.getText(), datePicker.getDate());
                        } catch (InvalidAmountException | DeptPaymentFailedException e) {
                                ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
                        } catch (PaymentIsGreaterThanRemaining e) {
                                WarningBox.show(this, e);
                        } finally {
                                debtController.closeFrameView(this);
                                try {
                                        debtController.refreshDebtView();
                                } catch (UpdatingModelFailed e) {
                                        ErrorBox.show(this, e);
                                }
                        }
                });

                repayAllButton.addActionListener(ae -> {
                        try {
                                debtController.payAll(debt, datePicker.getDate());
                                debtController.refreshDebtView();
                                debtController.closeFrameView(this);
                        } catch (DeptPaymentFailedException | UpdatingModelFailed e) {
                                ErrorBox.show(this, e);
                        }
                });

                cancelButton.addActionListener(ae -> debtController.closeFrameView(this));

                pack();
                setLocationRelativeTo(null);
        }

        private void initComponents() {
                JPanel upperPanel = new JPanel();
                GroupLayout upperLayout = new GroupLayout(upperPanel);
                upperPanel.setLayout(upperLayout);
                upperLayout.setAutoCreateContainerGaps(true);
                upperLayout.setAutoCreateGaps(true);
                add(upperPanel, BorderLayout.CENTER);

                FlowLayout lowerLayout = new FlowLayout(FlowLayout.CENTER);
                JPanel lowerPanel = new JPanel(lowerLayout);
                add(lowerPanel, BorderLayout.SOUTH);

                // Setup upper panel
                JLabel idTitleLabel = new JLabel("ID:");
                JLabel counterPartyNameTitleLabel = new JLabel("Name:");
                JLabel amountTitleLabel = new JLabel("Pay Amount:");
                JLabel dateTitleLabel = new JLabel("Date:");
                JLabel repayedTitleLabel = new JLabel("Already Repayed:");
                JLabel remainingTitleLabel = new JLabel("Remaining Debt:");

                Money repayed = debt.getPayedAmount();
                Money remainingDebt = new Money(debt.getDebtAmount().getAmount() - repayed.getAmount(),
                                Currency.getInstance("HUF"));

                JLabel idLabel = new JLabel(Long.toString(debt.getId()));
                JLabel counterPartyLabel = new JLabel(debt.getDebtor().getName());
                amountTextField = new JTextField("", 20);
                datePicker = new DatePicker();
                datePicker.setDate(LocalDate.now());
                JLabel repayedLabel = new JLabel(repayed.toString());
                JLabel remainingLabel = new JLabel(remainingDebt.toString());

                // Adding upper elements
                GroupLayout.SequentialGroup hGroup = upperLayout.createSequentialGroup();

                hGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(idTitleLabel)
                                .addComponent(counterPartyNameTitleLabel)
                                .addComponent(amountTitleLabel)
                                .addComponent(dateTitleLabel)
                                .addComponent(repayedTitleLabel)
                                .addComponent(remainingTitleLabel));

                hGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(idLabel)
                                .addComponent(counterPartyLabel)
                                .addComponent(amountTextField)
                                .addComponent(datePicker)
                                .addComponent(repayedLabel)
                                .addComponent(remainingLabel));

                upperLayout.setHorizontalGroup(hGroup);

                GroupLayout.SequentialGroup vGroup = upperLayout.createSequentialGroup();

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(idTitleLabel)
                                .addComponent(idLabel));

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(counterPartyNameTitleLabel)
                                .addComponent(counterPartyLabel));

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(amountTitleLabel)
                                .addComponent(amountTextField));

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(dateTitleLabel)
                                .addComponent(datePicker));

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(repayedTitleLabel)
                                .addComponent(repayedLabel));

                vGroup.addGroup(upperLayout.createParallelGroup()
                                .addComponent(remainingTitleLabel)
                                .addComponent(remainingLabel));

                upperLayout.setVerticalGroup(vGroup);

                // Setup lower panel
                repayButton = new JButton("Repay");
                lowerPanel.add(repayButton);

                repayAllButton = new JButton("Repay Remaining");
                lowerPanel.add(repayAllButton);

                cancelButton = new JButton("Cancel");
                lowerPanel.add(cancelButton);
        }
}
