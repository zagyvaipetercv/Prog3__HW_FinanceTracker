package financetracker.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import financetracker.datatypes.PurchasedItem;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;

/**
 * Table model for purchases
 * <p>
 * Model is uneditable. Contains information about the purches' id; date; and its PurchsaedItems' Category; name; price/unit; amount; summarized price 
 */
public class DetailedPurchaseTableModel extends AbstractTableModel {
    private List<PurchasedItem> items;

    private static final String[] COLUMN_NAMES = {
            "Purchase ID",
            "Date",
            "Category",
            "Name",
            "Price/Unit",
            "Amount",
            "Sum Price"
    };

    public DetailedPurchaseTableModel(List<PurchasedItem> items) {
        this.items = new ArrayList<>(items);
    }

    public DetailedPurchaseTableModel() {
        this.items = new ArrayList<>();
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
                return item.getPurchase().getId();
            case 1:
                return item.getPurchase().getDateOfPurchase();
            case 2:
                return item.getCategory();
            case 3:
                return item.getName();
            case 4:
                return item.getPricePerUnit();
            case 5:
                return item.getAmount();
            case 6:
                return item.getSumPrice();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return LocalDate.class;
            case 2:
                return Category.class;
            case 3:
                return String.class;
            case 4:
                return Money.class;
            case 5:
                return Double.class;
            case 6:
                return Money.class;
            default:
                return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
