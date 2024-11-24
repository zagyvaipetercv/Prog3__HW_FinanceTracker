package financetracker.models;

import java.util.List;

import financetracker.datatypes.PurchasedItem;

public class UneditablePurchasedItemTableModel extends PurchasedItemTableModel {

    public UneditablePurchasedItemTableModel(List<PurchasedItem> itemList) {
        super(itemList);
    }

    @Override
    protected void addEmptyRow() {
        // Do nothing
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    } 
}
