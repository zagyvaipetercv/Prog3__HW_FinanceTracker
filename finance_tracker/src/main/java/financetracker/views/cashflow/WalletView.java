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
import javax.swing.table.AbstractTableModel;

import financetracker.controllers.CashFlowController;
import financetracker.controllers.CashFlowController.CashFlowType;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.models.CashFlow;
import financetracker.models.Money;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;

public class WalletView extends PanelView {
    private static final int RIGHT_PANEL_WIDTH = 250;
    private static final Color BORDER_COLOR = Color.GRAY;
    private static final int BORDER_THICKNESS = 1;

    private CashFlowPanel cashFlowPanel;
    private SummaryPanel summaryPanel;

    private CashFlowController cashFlowController;

    public WalletView(CashFlowController cashFlowController) {
        this.cashFlowController = cashFlowController;

        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.EAST);

        int year = cashFlowController.getSelectedYear();
        Month month = cashFlowController.getSelectedMonth();

        // Get Data for Panels
        try {
            cashFlowPanel = new CashFlowPanel(
                    cashFlowController.getCashFlows(year, month, CashFlowType.ALL));

            summaryPanel = new SummaryPanel(
                    cashFlowController.getMoneyOnAccount(),
                    cashFlowController.getSummarizedCashFlow(year, month, CashFlowType.INCOME),
                    cashFlowController.getSummarizedCashFlow(year, month, CashFlowType.EXPENSE),
                    cashFlowController.getSummarizedCashFlow(year, month, CashFlowType.ALL));

        } catch (ControllerCannotReadException e) {
            ErrorBox.show("ERROR", e.getMessage());
        }

        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(cashFlowPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.NORTH);
        rightPanel.add(new OptionsPanel(), BorderLayout.CENTER);

    }

    private class FilterPanel extends JPanel {
        public FilterPanel() {
            // Setup panel
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));

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
                String yearString = yearTextField.getText();
                Month month = (Month) monthPicker.getSelectedItem();
                CashFlowType type = (CashFlowType) typePicker.getSelectedItem();

                try {
                    // Get Datas
                    cashFlowController.setFilterOptions(yearString, month, type);
                    cashFlowController.refreshWalletView();
                } catch (ControllerCannotReadException e) {
                    ErrorBox.show("ERROR", e.getMessage());
                } catch (InvalidYearFormatException e) {
                    ErrorBox.show(e.getErrorTitle(), e.getMessage());
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

        private CashFlowTableModel model;

        public CashFlowPanel(List<CashFlow> cashFlow) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));

            model = new CashFlowTableModel(cashFlow);
            JTable table = new JTable(model);
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
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
            setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, PANEL_HEIGHT));
            GridLayout layout = new GridLayout(4,2);
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

    private class OptionsPanel extends JPanel {
        private static final int PANEL_HEIGHT = 30;

        public OptionsPanel() {
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
            setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 0));
            FlowLayout layout = new FlowLayout();
            layout.setAlignOnBaseline(true);
            setLayout(layout);

            OptionButton changeMoneyButton = new OptionButton("Cahnge Money on account");
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
                setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, BUTTON_HEIGHT));
            }
        }
    }

    private class CashFlowTableModel extends AbstractTableModel {

        private List<CashFlow> cashFlowList;
        private final String[] columnNames = { "Date", "Amount", "Currency", "Reason" };

        public CashFlowTableModel(List<CashFlow> cashFlowList) {
            this.cashFlowList = cashFlowList;
        }

        @Override
        public int getRowCount() {
            return cashFlowList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CashFlow cashFlow = cashFlowList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return cashFlow.getDate();
                case 1:
                    return cashFlow.getMoney().getAmount();
                case 2:
                    return cashFlow.getMoney().getCurrency().getCurrencyCode(); // or getSymbol() for the currency
                                                                                // symbol
                case 3:
                    return cashFlow.getReason();
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return LocalDate.class;
                case 1:
                    return Double.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        private void setCashFlowList(List<CashFlow> moneyList) {
            this.cashFlowList = moneyList;
            fireTableDataChanged();
        }
    }
}
