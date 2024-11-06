package financetracker.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import financetracker.Main;
import financetracker.controllers.MoneyController;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.models.Money;
import financetracker.windowing.ErrorBox;

public class WalletView extends PanelView {
    private static final int RIGHT_PANEL_WIDTH = 250;
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final int BORDER_THICKNESS = 1;

    private CashFlowPanel cashFlowPanel;
    private SummaryPanel summaryPanel;

    public WalletView() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.EAST);

        LocalDate deafultDate = LocalDate.now();
        try {
            cashFlowPanel = new CashFlowPanel(Main.getMoneyController().getMoney(deafultDate.getYear(), deafultDate.getMonth()));
        } catch (ControllerCannotReadException e) {
            ErrorBox.show("ERROR", e.getMessage());
        }
        summaryPanel = new SummaryPanel();

        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(cashFlowPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.NORTH);
        rightPanel.add(new OptionsPanel(), BorderLayout.CENTER);

    }

    private class FilterPanel extends JPanel {
        private enum CashFlowType {
            ALL,
            INCOME,
            EXPENSE
        }

        public FilterPanel() {
            // Setup panel
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS));
            
            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            // Setup Components
            JLabel typeLabel = new JLabel("Type:");
            
            JComboBox<CashFlowType> typePicker = new JComboBox<>(CashFlowType.values());

            JLabel yearLabel = new JLabel("Year:");
            JTextField yearTextField = new JTextField(4);

            JLabel monthLabel = new JLabel("Month:");
            JComboBox<Month> monthPicker = new JComboBox<>(Month.values());

            JButton submitButton = new JButton("Filter");
            submitButton.addActionListener(ae -> {
                // TODO: Implement filtering
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

        public CashFlowPanel(List<Money> cashFlow) {
            setLayout(new BorderLayout());

            MoneyTableModel model = new MoneyTableModel(cashFlow);
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane);
        }
    }

    private class SummaryPanel extends JPanel {
        private static final int PANEL_HEIGHT = 100;

        public SummaryPanel() {
            setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(Color.RED);
        }
    }

    private class OptionsPanel extends JPanel {
        private static final int PANEL_HEIGHT = 30;

        public OptionsPanel() {
            setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 0));
            setBackground(Color.WHITE);
        }
    }

    private class MoneyTableModel extends AbstractTableModel {

        private final List<Money> moneyList;
        private final String[] columnNames = {"Date", "Amount", "Currency", "Reason"};

        public MoneyTableModel(List<Money> moneyList) {
            this.moneyList = moneyList;
        }

        @Override
        public int getRowCount() {
            return moneyList.size();
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
            Money money = moneyList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return money.getDate();
                case 1:
                    return money.getAmount();
                case 2:
                    return money.getCurrency().getCurrencyCode(); // or getSymbol() for the currency symbol
                case 3: 
                    return money.getReason();
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
    }
}
