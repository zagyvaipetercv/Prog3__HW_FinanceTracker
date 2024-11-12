package financetracker.views.cashflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import financetracker.controllers.CashFlowController;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MyWindowConstants;
import financetracker.windowing.OptionsPanel;
import financetracker.models.CashFlowTableModel;

public class WalletView extends PanelView {


    private CashFlowController cashFlowController;

    public WalletView(
            CashFlowController cashFlowController,
            CashFlowTableModel tm,
            Money incomes, Money expneses, Money thisMonth, Money all) {
        this.cashFlowController = cashFlowController;

        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.EAST);

        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(new CashFlowPanel(tm), BorderLayout.CENTER);
        rightPanel.add(new SummaryPanel(all, incomes, expneses, thisMonth), BorderLayout.NORTH);
        
        OptionsPanel optionsPanel = new OptionsPanel();
        optionsPanel.addOptionButton(
            "Change Money on account",
            ae -> cashFlowController.getChangeMoneyView().setVisible(true));

        optionsPanel.addOptionButton(
            "Set Money on account",
            ae -> cashFlowController.getSetMoneyView().setVisible(true));

        rightPanel.add(optionsPanel, BorderLayout.CENTER);

    }

    private class FilterPanel extends JPanel {
        public FilterPanel() {
            // Setup panel
            setBorder(BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            // Setup Components
            JLabel typeLabel = new JLabel("Type:");
            JComboBox<CashFlowController.CashFlowType> typePicker = new JComboBox<>(
                    CashFlowController.CashFlowType.values());
            typePicker.setSelectedItem(cashFlowController.getSelectedCashFlowType());

            JLabel yearLabel = new JLabel("Year:");
            JTextField yearTextField = new JTextField(4);
            yearTextField.setText(String.valueOf(cashFlowController.getSelectedYear()));

            JLabel monthLabel = new JLabel("Month:");
            JComboBox<Month> monthPicker = new JComboBox<>(Month.values());
            monthPicker.setSelectedItem(cashFlowController.getSelectedMonth());

            JButton submitButton = new JButton("Filter");
            submitButton.addActionListener(ae -> {
                try {
                    cashFlowController.setFilterOptions(
                        yearTextField.getText(), 
                        (Month) monthPicker.getSelectedItem(),
                        (CashFlowController.CashFlowType)typePicker.getSelectedItem());
                } catch (InvalidYearFormatException e) {
                    ErrorBox.show(e.getErrorTitle(), e.getMessage());
                } catch (SerializerCannotRead e) {
                    ErrorBox.show("ERROR", e.getMessage());
                }
            });

            // Add Components
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(typeLabel)
                    .addComponent(typePicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(yearLabel)
                    .addComponent(yearTextField));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(monthLabel)
                    .addComponent(monthPicker));

            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(submitButton));

            layout.setHorizontalGroup(hGroup);

            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(typeLabel)
                    .addComponent(yearLabel)
                    .addComponent(monthLabel));

            vGroup.addGroup(layout.createParallelGroup()
                    .addComponent(typePicker)
                    .addComponent(yearTextField)
                    .addComponent(monthPicker)
                    .addComponent(submitButton));

            layout.setVerticalGroup(vGroup);
        }
    }

    private class CashFlowPanel extends JPanel {

        public CashFlowPanel(CashFlowTableModel tm) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            JTable table = new JTable(tm);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane);
        }
    }

    private class SummaryPanel extends JPanel {
        private static final int PANEL_HEIGHT = 120;

        JLabel moneyOnAcountLabel;
        JLabel sumIncomeLabel;
        JLabel sumExpenseLabel;
        JLabel selectedMonthSumLabel;

        public SummaryPanel(Money onAccount, Money sumIncome, Money sumExpense, Money selectedMonthSum) {
            setBorder(BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));
            setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, PANEL_HEIGHT));
            GridLayout layout = new GridLayout(4, 2);
            setLayout(layout);

            JLabel moneyOnAccountTitleLabel = new JLabel("Money on account:");
            add(moneyOnAccountTitleLabel);
            moneyOnAcountLabel = new JLabel(onAccount.toString());
            add(moneyOnAcountLabel);

            JLabel selectedMonthSumTitleLabel = new JLabel("Selected month sum:");
            add(selectedMonthSumTitleLabel);
            selectedMonthSumLabel = new JLabel(selectedMonthSum.toString());
            add(selectedMonthSumLabel);

            JLabel incomeTitleLable = new JLabel("Income:");
            add(incomeTitleLable);
            sumIncomeLabel = new JLabel(sumIncome.toString());
            add(sumIncomeLabel);

            JLabel expenseTitleLable = new JLabel("Expenses:");
            add(expenseTitleLable);
            sumExpenseLabel = new JLabel(sumExpense.toString());
            add(sumExpenseLabel);
        }
    }

    /*
    private class OptionsPanel extends JPanel {
        public OptionsPanel() {
            setBorder(BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));
            setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, 0));
            FlowLayout layout = new FlowLayout();
            layout.setAlignOnBaseline(true);
            setLayout(layout);
            
            OptionButton changeMoneyButton = new OptionButton("Change Money on account");
            changeMoneyButton.addActionListener(ae -> cashFlowController.getChangeMoneyView().setVisible(true));
            add(changeMoneyButton);
            
            OptionButton setMoneyButton = new OptionButton("Set Money on account");
            setMoneyButton.addActionListener(ae -> cashFlowController.getSetMoneyView().setVisible(true));
            add(setMoneyButton);
        }

        private class OptionButton extends JButton {
            private static final int BUTTON_HEIGHT = 30;
            
            public OptionButton(String text) {
                super(text);
                setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, BUTTON_HEIGHT));
            }
        }
    }
    */
}
