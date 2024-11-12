package financetracker.views.debt;

import java.time.LocalDate;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.DebtController;
import financetracker.datatypes.Debt.DebtDirection;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.debtcontroller.CreatingDebtFailedException;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

public class AddNewDebtView extends FrameView {
    public AddNewDebtView(DebtController debtController) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Finance Tracker - Add Debt");

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel counterPartyLabel = new JLabel("Counterparty name:");
        JLabel directionLabel = new JLabel("Dircetion:");
        JLabel dateLabel = new JLabel("Date:");
        JLabel amountLabel = new JLabel("Amount:");
        JLabel hasDeadlineLabel = new JLabel("Has a deadline:");
        JLabel deadLineLabel = new JLabel("Date of deadline");
        deadLineLabel.setEnabled(false);

        JTextField nameTextField = new JTextField("", 20);
        JComboBox<DebtDirection> directionPicker = new JComboBox<>(DebtDirection.values());
        DatePicker datePicker = new DatePicker();
        datePicker.setDate(LocalDate.now());
        JTextField amountTextField = new JTextField("", 20);

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setEnabled(false);
        deadlinePicker.setDate(LocalDate.now());

        JCheckBox hasDeadLinechCheckBox = new JCheckBox(" has deadline", false);
        hasDeadLinechCheckBox.addChangeListener(ae -> {
            boolean isSelected = hasDeadLinechCheckBox.isSelected();
            deadlinePicker.setEnabled(isSelected);
            deadLineLabel.setEnabled(isSelected);
        });

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener( ae -> {
                try {
                        debtController.addDebt(
                                nameTextField.getText(),
                                (DebtDirection) directionPicker.getSelectedItem(),
                                datePicker.getDate(),
                                amountTextField.getText(),
                                hasDeadLinechCheckBox.isSelected(),
                                deadlinePicker.getDate());

                        debtController.closeFrameView(this);
                } catch (UserNotFound | InvalidAmountException | CreatingDebtFailedException e) {
                        ErrorBox.show(e.getErrorTitle(), e.getMessage());
                }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(ae -> debtController.closeFrameView(this)); 



        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(counterPartyLabel)
                .addComponent(directionLabel)
                .addComponent(dateLabel)
                .addComponent(amountLabel)
                .addComponent(hasDeadlineLabel)
                .addComponent(deadLineLabel)
                .addComponent(submitButton));

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(nameTextField)
                .addComponent(directionPicker)
                .addComponent(datePicker)
                .addComponent(amountTextField)
                .addComponent(hasDeadLinechCheckBox)
                .addComponent(deadlinePicker)
                .addComponent(cancelButton));

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(counterPartyLabel)
                .addComponent(nameTextField));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(directionLabel)
                .addComponent(directionPicker));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(dateLabel)
                .addComponent(datePicker));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(amountLabel)
                .addComponent(amountTextField));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(hasDeadlineLabel)
                .addComponent(hasDeadLinechCheckBox));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(deadLineLabel)
                .addComponent(deadlinePicker));

        vGroup.addGroup(layout.createParallelGroup()
                .addComponent(submitButton)
                .addComponent(cancelButton));

        layout.setVerticalGroup(vGroup);

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
