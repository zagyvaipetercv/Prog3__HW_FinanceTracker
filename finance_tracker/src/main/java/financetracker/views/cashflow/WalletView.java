package financetracker.views.cashflow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.Month;

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
import financetracker.controllers.CashFlowController.CashFlowType;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MyWindowConstants;
import financetracker.windowing.OptionsPanel;
import financetracker.models.CashFlowTableModel;
import financetracker.models.SummarizedCashFlowModel;

/**
 * PanelView where the user can manage their cashflows
 */
public class WalletView extends PanelView {

    private CashFlowController cashFlowController;

    private CashFlowTableModel cashFlowTableModel;
    private SummarizedCashFlowModel summarizedCashFlowModel;

    private int year;
    private Month month;
    private CashFlowType cashFlowType;

    public WalletView(
            CashFlowController cashFlowController,
            CashFlowTableModel cashFlowTableModel, SummarizedCashFlowModel summarizedCashFlowModel,
            int year, Month month, CashFlowType cashFlowType) {

        this.cashFlowController = cashFlowController;
        this.cashFlowTableModel = cashFlowTableModel;
        this.summarizedCashFlowModel = summarizedCashFlowModel;

        this.year = year;
        this.month = month;
        this.cashFlowType = cashFlowType;

        initCompoents();

    }

    private void initCompoents() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.EAST);

        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(new CashFlowPanel(), BorderLayout.CENTER);
        rightPanel.add(new SummaryPanel(), BorderLayout.NORTH);

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
            typePicker.setSelectedItem(cashFlowType);

            JLabel yearLabel = new JLabel("Year:");
            JTextField yearTextField = new JTextField(4);
            yearTextField.setText(String.valueOf(year));

            JLabel monthLabel = new JLabel("Month:");
            JComboBox<Month> monthPicker = new JComboBox<>(Month.values());
            monthPicker.setSelectedItem(month);

            JButton submitButton = new JButton("Filter");
            submitButton.addActionListener(ae -> {
                try {
                    cashFlowController.filterFor(
                            yearTextField.getText(),
                            (Month) monthPicker.getSelectedItem(),
                            (CashFlowController.CashFlowType) typePicker.getSelectedItem());

                    cashFlowController.refreshWalletView();
                } catch (InvalidYearFormatException | UpdatingModelFailed e) {
                    ErrorBox.show(this, e.getErrorTitle(), e.getMessage());
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

        public CashFlowPanel() {
            setLayout(new BorderLayout());
            setBorder(
                    BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            JTable table = new JTable(cashFlowTableModel);
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

        public SummaryPanel() {
            setBorder(
                    BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));
            setPreferredSize(new Dimension(MyWindowConstants.OPTIONS_PANEL_WIDTH, PANEL_HEIGHT));
            GridLayout layout = new GridLayout(4, 2);
            setLayout(layout);

            JLabel moneyOnAccountTitleLabel = new JLabel("Money on account:");
            add(moneyOnAccountTitleLabel);
            moneyOnAcountLabel = new JLabel(summarizedCashFlowModel.getMoneyOnAccount().toString());
            add(moneyOnAcountLabel);

            JLabel selectedMonthSumTitleLabel = new JLabel("Selected month sum:");
            add(selectedMonthSumTitleLabel);
            selectedMonthSumLabel = new JLabel(summarizedCashFlowModel.getSumThisMonth().toString());
            add(selectedMonthSumLabel);

            JLabel incomeTitleLable = new JLabel("Income:");
            add(incomeTitleLable);
            sumIncomeLabel = new JLabel(summarizedCashFlowModel.getSumIncomes().toString());
            add(sumIncomeLabel);

            JLabel expenseTitleLable = new JLabel("Expenses:");
            add(expenseTitleLable);
            sumExpenseLabel = new JLabel(summarizedCashFlowModel.getSumExpenses().toString());
            add(sumExpenseLabel);
        }
    }
}
