package financetracker.models;

import java.util.List;

import javax.swing.DefaultListModel;

import financetracker.datatypes.Purchase;

/**
 * List Model for purchases
 */
public class PurchaseListModel extends DefaultListModel<Purchase> {
    public PurchaseListModel(List<Purchase> purchases) {
        addAll(purchases);
    }

    public PurchaseListModel() {
        super();
    }
}
