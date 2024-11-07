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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import financetracker.Main;
import financetracker.controllers.CashFlowController;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.models.CashFlow;
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
            cashFlowPanel = new CashFlowPanel(
                    Main.getCashFlowController().getCashFlows(deafultDate.getYear(), deafultDate.getMonth()));
        } catch (ControllerCannotReadException e) {
            ErrorBox.show("ERROR", e.getMessage());
        }
        summaryPanel = new SummaryPanel();

        centerPanel.add(new FilterPanel(), BorderLayout.NORTH);
        centerPanel.add(cashFlowPanel, BorderLayout.CENTER);
        rightPanel.add(summaryPanel, BorderLayout.NORTH);
        rightPanel.add(new OptionsPanel(), BorderLayout.CENTER);

    }

    private void updateTable(List<CashFlow> newList) {
        cashFlowPanel.updateTable(newList);
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

            JComboBox<CashFlowController.CashFlowType> typePicker = new JComboBox<>(CashFlowController.CashFlowType.values());

            JLabel yearLabel = new JLabel("Year:");
            JTextField yearTextField = new JTextField(4);

            JLabel monthLabel = new JLabel("Month:");
            JComboBox<Month> monthPicker = new JComboBox<>(Month.values());

            JButton submitButton = new JButton("Filter");
            submitButton.addActionListener(ae -> {
                int year = Integer.parseInt(yearTextField.getText());
                Month month = (Month) monthPicker.getSelectedItem();

                try {
                    List<CashFlow> filteredList = Main.getCashFlowController().getCashFlows(year, month);
                    updateTable(filteredList);
                } catch (ControllerCannotReadException e) {
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

        private CashFlowTableModel model;

        public CashFlowPanel(List<CashFlow> cashFlow) {
            setLayout(new BorderLayout());

            model = new CashFlowTableModel(cashFlow);
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane);
        }

        private void updateTable(List<CashFlow> newList) {
            model.setCashFlowList(newList);
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
                    return cashFlow.getMoney().getCurrency().getCurrencyCode(); // or getSymbol() for the currency symbol
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
