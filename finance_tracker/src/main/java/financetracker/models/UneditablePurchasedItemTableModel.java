package financetracker.models;

import java.util.List;

import financetracker.datatypes.PurchasedItem;

/**
 * Inherits the PurchasedItemTableModel
 * <p>
 * overides:
 * <ul>
 * <li>the addEmptyRow() to do nothing</li>
 * <li>isCellEditable() to return false</li>
 * </ul>
 * 
 */
public class UneditablePurchasedItemTableModel extends PurchasedItemTableModel {

    public UneditablePurchasedItemTableModel(List<PurchasedItem> itemList) {
        super(itemList);
    }

    /**
     * Does nothing
     */
    @Override
    protected void addEmptyRow() {
        // Do nothing
    }

    /**
     * Always returns false
     * 
     * @return false 
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
