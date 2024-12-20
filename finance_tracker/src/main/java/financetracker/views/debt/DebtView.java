package financetracker.views.debt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import financetracker.controllers.DebtController;
import financetracker.controllers.DebtController.DebtDirection;
import financetracker.controllers.DebtController.DebtFulfilled;
import financetracker.datatypes.Debt;
import financetracker.datatypes.User;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.exceptions.generic.DeletingRecordFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.exceptions.models.NoItemWasSelected;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.models.DebtListModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MyWindowConstants;
import financetracker.windowing.OptionsPanel;

/**
 * PanelView where the user cane manage their debts
 */
public class DebtView extends PanelView {
    private JList<Debt> debts;

    private DebtController debtController;

    private DebtDirection direction;
    private DebtFulfilled isFulfilled;
    private User filteredUser;

    public DebtView(DebtController controller, DebtListModel dlm,
            DebtDirection direction, DebtFulfilled isFulfilled, User filteredUser) {
        this.debtController = controller;

        this.direction = direction;
        this.isFulfilled = isFulfilled;
        this.filteredUser = filteredUser;

        setLayout(new BorderLayout());
        debts = new JList<>(dlm);

        debts.setCellRenderer(new DebtListCellRenderer());
        debts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel centerPanel = new JPanel(new BorderLayout());

        OptionsPanel optionsPanel = new OptionsPanel();
        optionsPanel.addOptionButton(
                "Add New Debt",
                ae -> controller.getAddNewDebtView().setVisible(true));
        optionsPanel.addOptionButton(
                "Edit Selected",
                ae -> {
                    try {
                        Debt selected = debts.getSelectedValue();
                        controller.getEditSelectedDebtView(selected).setVisible(true);
                    } catch (NoItemWasSelected | FulfilledDebtCantChange e) {
                        ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
                    }
                });
        optionsPanel.addOptionButton(
                "Add Payment to Selected",
                ae -> {
                    try {
                        Debt selected = debts.getSelectedValue();
                        controller.getAddPaymentView(selected).setVisible(true);
                    } catch (NoItemWasSelected | FulfilledDebtCantChange e) {
                        ErrorBox.show(this, e);
                    }
                });

        optionsPanel.addOptionButton(
                "Delete Debt",
                ae -> {
                    try {
                        controller.deleteDebt(debts.getSelectedValue());
                        controller.refreshDebtView();
                    } catch (DeletingRecordFailed | UpdatingModelFailed | NoItemWasSelected e) {
                        ErrorBox.show(this, e);
                    }
                });
        add(optionsPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(debts);
        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * The efault list cell renderer for debts 
     */
    private class DebtListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected,
                boolean hasFocus) {

            JPanel panel = new JPanel(new GridLayout(1, 7, 0, 0));
            panel.setPreferredSize(new Dimension(0, MyWindowConstants.LIST_CELL_PREFFERED_HEIGHT));

            Debt debt = (Debt) value;

            JLabel idLabel = new JLabel("ID: " + debt.getId());
            setComponentAttributes(idLabel);
            panel.add(idLabel);

            JLabel owesLabel;
            DebtDirection direction = debtController.getDirection(debt);
            switch (direction) {
                case I_OWE:
                    owesLabel = new JLabel("You owe " + debt.getCreditor().getName());
                    break;

                case THEY_OWE:
                    owesLabel = new JLabel(debt.getDebtor().getName() + " owes you");
                    break;

                default:
                    owesLabel = new JLabel("UNSET DEBT DIRECTION");
                    break;
            }
            setComponentAttributes(owesLabel);
            panel.add(owesLabel);

            JLabel amountLabel = new JLabel("Amount: " + debt.getDebtAmount());
            setComponentAttributes(amountLabel);
            panel.add(amountLabel);

            JLabel dateLabel = new JLabel("Date: " + debt.getDate().toString());
            setComponentAttributes(dateLabel);
            panel.add(dateLabel);

            JLabel deadlineLabel = new JLabel(
                    "Deadline: " + (debt.hasDeadline() ? debt.getDeadline().toString() : "-"));
            setComponentAttributes(deadlineLabel);
            panel.add(deadlineLabel);

            JLabel repayedAmountLabel = new JLabel("Repayed: " + debt.getPayedAmount().toString());
            setComponentAttributes(repayedAmountLabel);
            panel.add(repayedAmountLabel);

            JCheckBox fullfilledCheckBox = new JCheckBox("Fullfilled");
            fullfilledCheckBox.setSelected(debt.isFulfilled());
            setComponentAttributes(fullfilledCheckBox);
            panel.add(fullfilledCheckBox);

            // List View Selection stuff
            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                panel.setForeground(list.getSelectionForeground());
                fullfilledCheckBox.setBackground(list.getSelectionBackground());
                fullfilledCheckBox.setForeground(list.getSelectionForeground());
            } else {
                panel.setBackground(list.getBackground());
                panel.setForeground(list.getForeground());
                fullfilledCheckBox.setBackground(list.getBackground());
                fullfilledCheckBox.setForeground(list.getForeground());
            }

            panel.setBorder(
                    BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            return panel;
        }

        private static void setComponentAttributes(JLabel component) {
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }

        private static void setComponentAttributes(JCheckBox component) {
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private class FilterPanel extends JPanel {
        public FilterPanel() {
            // Setup panel
            setBorder(
                    BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            // Setup Components
            JLabel directionLabel = new JLabel("Direction:");
            JComboBox<DebtDirection> directionPicker = new JComboBox<>(
                    DebtDirection.values());
            directionPicker.setSelectedItem(direction);

            JLabel fulfilledLabel = new JLabel("Fulfilled");
            JComboBox<DebtFulfilled> fulfilledPicker = new JComboBox<>(DebtFulfilled.values());
            fulfilledPicker.setSelectedItem(isFulfilled);

            JLabel userLabel = new JLabel("User:");
            JTextField userTextField = new JTextField("", 20);
            userTextField.setText((filteredUser == null ? "" : filteredUser.getName()));

            JButton submitButton = new JButton("Filter");
            submitButton.addActionListener(ae -> {
                try {
                    debtController.filterFor((DebtDirection) directionPicker.getSelectedItem(),
                            (DebtFulfilled) fulfilledPicker.getSelectedItem(), userTextField.getText());
                    debtController.refreshDebtView();
                } catch (UserNotFound | UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
            });

            // Add Components
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(directionLabel)
                    .addComponent(directionPicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(fulfilledLabel)
                    .addComponent(fulfilledPicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(userLabel)
                    .addComponent(userTextField));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(submitButton));

            layout.setHorizontalGroup(hGroup);

            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(directionLabel)
                    .addComponent(fulfilledLabel)
                    .addComponent(userLabel));

            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(directionPicker)
                    .addComponent(fulfilledPicker)
                    .addComponent(userTextField)
                    .addComponent(submitButton));

            layout.setVerticalGroup(vGroup);
        }
    }
}
