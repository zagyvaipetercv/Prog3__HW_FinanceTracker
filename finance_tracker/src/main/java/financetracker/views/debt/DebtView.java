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
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import financetracker.controllers.DebtController;
import financetracker.datatypes.Debt;
import financetracker.models.DebtListModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.OptionsPanel;

public class DebtView extends PanelView {
    private JList<Debt> debts;

    private DebtController controller;

    public DebtView(DebtController controller, DebtListModel dlm) {
        this.controller = controller;

        setLayout(new BorderLayout());
        debts = new JList<>(dlm);

        debts.setCellRenderer(new DebtListCellRenderer());

        OptionsPanel optionsPanel = new OptionsPanel();
        optionsPanel.addOptionButton(
                "Add New Debt",
                ae -> {
                    controller.getAddNewDebtView().setVisible(true);
                });
        optionsPanel.addOptionButton("Edit Selected", null);
        optionsPanel.addOptionButton("Add Payment to Selected", null);
        add(optionsPanel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(debts);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class DebtListCellRenderer extends JPanel implements ListCellRenderer<Debt> {

        private static final int PREFFERED_HEIGHT = 50;

        public DebtListCellRenderer() {
            setLayout(new BorderLayout());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Debt> list, Debt debt, int index,
                boolean isSelected,
                boolean hasFocus) {

            JPanel panel = new JPanel(new GridLayout(1,6,0,0));
            panel.setPreferredSize(new Dimension(0,PREFFERED_HEIGHT));

            JLabel owesLabel;
            switch (debt.getDirection()) {
                case I_OWE:
                    owesLabel = new JLabel("You owe " + debt.getCounterParty().getName());
                    break;

                case THEY_OWE:
                    owesLabel = new JLabel(debt.getCounterParty().getName() + " owes you");
                    break;

                default:
                    owesLabel = new JLabel();
                    break;
            }
            setComponentAttributes(owesLabel);
            panel.add(owesLabel);


            JLabel amountLabel = new JLabel("Amount: " + debt.getAmount());
            setComponentAttributes(amountLabel);
            panel.add(amountLabel);

            JLabel dateLabel = new JLabel("Date: " + debt.getDate().toString());
            setComponentAttributes(dateLabel);
            panel.add(dateLabel);

            JLabel deadlineLabel = new JLabel(
                    "Deadline: " + (debt.hasDeadline() ? debt.getDeadline().toString() : "-"));
            setComponentAttributes(deadlineLabel);
            panel.add(deadlineLabel);

            JLabel repayedAmountLabel = new JLabel("Repayed: " + controller.repayed(debt).toString());
            setComponentAttributes(repayedAmountLabel);
            panel.add(repayedAmountLabel);

            JCheckBox fullfilledCheckBox = new JCheckBox("Fullfilled");
            fullfilledCheckBox.setSelected(debt.isFulfilled());
            setComponentAttributes(fullfilledCheckBox);
            panel.add(fullfilledCheckBox);


            add(panel, BorderLayout.CENTER);

            // List View Selection stuff
            if (isSelected) {
                panel.setBackground(list.getSelectionBackground());
                panel.setForeground(list.getSelectionForeground());
                fullfilledCheckBox.setBackground(list.getSelectionBackground());
                fullfilledCheckBox.setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                panel.setBackground(list.getBackground());
                panel.setForeground(list.getForeground());
                fullfilledCheckBox.setBackground(list.getBackground());
                fullfilledCheckBox.setForeground(list.getForeground());
            }

            return this;
        }

        private static void setComponentAttributes(JLabel component) {
            component.setBorder(BorderFactory.createLineBorder(Color.gray));
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }

        private static void setComponentAttributes(JCheckBox component) {
            component.setBorder(BorderFactory.createLineBorder(Color.gray));
            component.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
