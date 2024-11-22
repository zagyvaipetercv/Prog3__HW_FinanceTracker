package financetracker.models;

import java.util.List;

import javax.swing.DefaultListModel;

import financetracker.datatypes.Purchase;

public class PurchaseListModel extends DefaultListModel<Purchase> {
    public PurchaseListModel(List<Purchase> purchases) {
        addAll(purchases);
    }
}
