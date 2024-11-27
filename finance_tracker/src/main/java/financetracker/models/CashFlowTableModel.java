package financetracker.models;

import java.time.LocalDate;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import financetracker.datatypes.CashFlow;

/**
 * Table model for a the cashflow datatype
 * <p>
 * Inherits the AbstractTableModel
 */
public class CashFlowTableModel extends AbstractTableModel {

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
}