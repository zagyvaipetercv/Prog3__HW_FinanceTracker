package financetracker.views.debt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import financetracker.controllers.DebtController;
import financetracker.datatypes.Debt;
import financetracker.datatypes.Debt.DebtDirection;
import financetracker.exceptions.NoItemWasSelected;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.models.DebtListModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.OptionsPanel;

public class DebtView extends PanelView {
    private JList<Debt> debts;

    private DebtController controller;

    public DebtView(DebtController controller, DebtListModel dlm) {
        this.controller = controller;

        setLayout(new BorderLayout());
        debts = new JList<>(dlm);

        debts.setCellRenderer(new DebtListCellRenderer());
        debts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        OptionsPanel optionsPanel = new OptionsPanel();
        optionsPanel.addOptionButton(
                "Add New Debt",
                ae -> {
                    controller.getAddNewDebtView().setVisible(true);
                });
        optionsPanel.addOptionButton(
                "Edit Selected",
                ae -> {
                    try {
                        controller.getEditSelectedDebtView(debts).setVisible(true);
                    } catch (NoItemWasSelected | FulfilledDebtCantChange e) {
                        ErrorBox.show(e.getErrorTitle(), e.getMessage());
                    }
                });
        optionsPanel.addOptionButton(
                "Add Payment to Selected",
                ae -> {
                    try {
                        controller.getAddPaymentView(debts).setVisible(true);
                    } catch (NoItemWasSelected | FulfilledDebtCantChange e) {
                        ErrorBox.show(e.getErrorTitle(), e.getMessage());
                    }
                });
        add(optionsPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(debts);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class DebtListCellRenderer extends DefaultListCellRenderer {

        private static final int PREFFERED_HEIGHT = 50;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected,
                boolean hasFocus) {

            JPanel panel = new JPanel(new GridLayout(1, 7, 0, 0));
            panel.setPreferredSize(new Dimension(0, PREFFERED_HEIGHT));

            Debt debt = (Debt) value;

            JLabel idLabel = new JLabel("ID: " + debt.getId());
            setComponentAttributes(idLabel);
            panel.add(idLabel);

            JLabel owesLabel;
            DebtDirection direction = controller.getDirection(debt);
            switch (direction) {
                case I_OWE:
                    owesLabel = new JLabel("You owe " + debt.getDebtor().getName());
                    break;

                case THEY_OWE:
                    owesLabel = new JLabel(debt.getDebtor().getName() + " owes you");
                    break;

                default:
                    owesLabel = new JLabel();
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

            JLabel repayedAmountLabel = new JLabel("Repayed: " + Debt.repayed(debt).toString());
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

            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            return panel;
        }

        private static void setComponentAttributes(JLabel component) {
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }

        private static void setComponentAttributes(JCheckBox component) {
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
