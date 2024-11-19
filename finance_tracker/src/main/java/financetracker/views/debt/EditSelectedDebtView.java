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
import financetracker.datatypes.Debt;
import financetracker.datatypes.Debt.DebtDirection;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.debtcontroller.EditingDebtFailedException;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

public class EditSelectedDebtView extends FrameView {

    private DebtController debtController;

    private Debt debt;

    private JTextField nameTextField;
    private JComboBox<DebtDirection> directionPicker;
    private DatePicker datePicker;
    private JTextField amountTextField;
    private JCheckBox hasDeadLinechCheckBox;
    private DatePicker deadlinePicker;

    private JButton submitButton;

    public EditSelectedDebtView(DebtController controller, Debt debt) {
        this.debtController = controller;
        this.debt = debt;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Finance Tracker - Edit Debt - " + debt.getId());

        initComponents();
        setDefaultValues();

        submitButton.addActionListener(ae -> {
            try {
                debtController.editDebt(
                        debt,
                        nameTextField.getText(),
                        (DebtDirection) directionPicker.getSelectedItem(),
                        datePicker.getDate(),
                        amountTextField.getText(),
                        hasDeadLinechCheckBox.isSelected(),
                        deadlinePicker.getDate());

                debtController.refreshDebtView();
                debtController.closeFrameView(this);
            } catch (InvalidAmountException | UserNotFound | EditingDebtFailedException e) {
                ErrorBox.show(e.getErrorTitle(), e.getMessage());
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void setDefaultValues() {
        nameTextField.setText(debt.getDebtor().getName());
        DebtDirection direction = debtController.getDirection(debt);
        directionPicker.setSelectedItem(direction);
        datePicker.setDate(debt.getDate());
        amountTextField.setText(((Double) debt.getDebtAmount().getAmount()).toString());
        hasDeadLinechCheckBox.setSelected(debt.hasDeadline());
        deadlinePicker.setDate((debt.hasDeadline() ? debt.getDeadline() : LocalDate.now()));
    }

    private void initComponents() {
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

        nameTextField = new JTextField("", 20);
        directionPicker = new JComboBox<>(DebtDirection.values());
        datePicker = new DatePicker();
        datePicker.setDate(LocalDate.now());
        amountTextField = new JTextField("", 20);

        deadlinePicker = new DatePicker();
        deadlinePicker.setEnabled(false);
        deadlinePicker.setDate(LocalDate.now());

        hasDeadLinechCheckBox = new JCheckBox(" has deadline", false);
        hasDeadLinechCheckBox.addChangeListener(ae -> {
            boolean isSelected = hasDeadLinechCheckBox.isSelected();
            deadlinePicker.setEnabled(isSelected);
            deadLineLabel.setEnabled(isSelected);
        });

        submitButton = new JButton("Submit");

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
    }
}
