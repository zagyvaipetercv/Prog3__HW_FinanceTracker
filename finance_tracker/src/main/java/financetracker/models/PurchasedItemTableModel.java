package financetracker.models;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import financetracker.datatypes.BoughtItem;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;

public class PurchasedItemTableModel extends AbstractTableModel {

    private List<BoughtItem> items;

    private static final String[] COLUMN_NAMES = { "Category", "Name", "Price/Unit", "Amount", "Sum Price" };

    public PurchasedItemTableModel(List<BoughtItem> items) {
        this.items = new ArrayList<>(items);
        addEmptyRow();
    }

    public PurchasedItemTableModel() {
        this.items = new ArrayList<>();
        addEmptyRow();
    }

    public List<BoughtItem> getItems() {
        return items.subList(0, items.size() - 1);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BoughtItem item = items.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return item.getCategory();
            case 1:
                return item.getName();
            case 2:
                return item.getPricePerUnit();
            case 3:
                return item.getAmount();
            case 4:
                return item.getSumPrice();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Category.class;
            case 1:
                return String.class;
            case 2:
                return Money.class;
            case 3:
                return Double.class;
            case 4:
                return Money.class;
            default:
                return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
        BoughtItem item = items.get(rowIndex);

        switch (columnIndex) {
            case 0:
                item.setCategory((Category) newValue);
                break;

            case 1:
                item.setName((String) newValue);
                break;

            case 2:
                item.setPricePerUnit((Money) newValue);
                break;

            case 3:
                item.setAmount((Double) newValue);
                break;

            case 4:
                item.setSumPrice((Money) newValue);
                break;

            default:
                break;
        }

        fireTableCellUpdated(rowIndex, columnIndex);
        if (isRowFilled(rowIndex) && rowIndex == items.size() - 1) {
            addEmptyRow();
            fireTableRowsInserted(items.size() - 1, items.size() - 1);
        }
    }

    private boolean isRowFilled(int rowIndex) {
        BoughtItem row = items.get(rowIndex);
        return (!row.getCategory().getName().isBlank() &&
                !row.getName().isBlank() &&
                row.getPricePerUnit().getAmount() != 0.0 &&
                row.getAmount() != 0 &&
                row.getSumPrice().getAmount() != 0.0);
    }

    private void addEmptyRow() {
        items.add(new BoughtItem(new Category(-1, ""), "", new Money(0, Currency.getInstance("HUF")), 0));
    }

    public void deleteRow(int rowIndex) {
        items.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
