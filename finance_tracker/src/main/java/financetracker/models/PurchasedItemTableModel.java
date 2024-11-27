package financetracker.models;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import financetracker.datatypes.PurchasedItem;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;

/**
 * Table model for the purchased items
 * <p>
 * Checks if last row is filled with data. If so then automatically opens a new empty row.
 * <p>
 * Cells are editable
 */
public class PurchasedItemTableModel extends AbstractTableModel {

    private List<PurchasedItem> items;

    private static final String[] COLUMN_NAMES = { "Category", "Name", "Price/Unit", "Amount", "Sum Price" };

    public PurchasedItemTableModel(List<PurchasedItem> items) {
        this.items = new ArrayList<>(items);
        addEmptyRow();
    }

    public PurchasedItemTableModel() {
        this.items = new ArrayList<>();
        addEmptyRow();
    }

    public List<PurchasedItem> getItems() {
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
        PurchasedItem item = items.get(rowIndex);

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
        PurchasedItem item = items.get(rowIndex);

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

    /**
     * Checks if the row is filled with data
     * @param rowIndex the row that will be checked
     * @return true if every cell in the row is filled with data. false if not
     */
    private boolean isRowFilled(int rowIndex) {
        PurchasedItem row = items.get(rowIndex);
        return (!row.getCategory().getName().isBlank() &&
                !row.getName().isBlank() &&
                row.getPricePerUnit().getAmount() != 0.0 &&
                row.getAmount() != 0 &&
                row.getSumPrice().getAmount() != 0.0);
    }

    /**
     * Adds an empty row to the end of the model
     */
    protected void addEmptyRow() {
        items.add(new PurchasedItem(null, new Category(-1, ""), "", new Money(0, Currency.getInstance("HUF")), 0));
    }

    /**
     * Deletes a row from the list and updates the model
     * @param rowIndex the row that will be deleted
     */
    public void deleteRow(int rowIndex) {
        items.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
