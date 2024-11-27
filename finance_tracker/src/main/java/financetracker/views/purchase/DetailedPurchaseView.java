package financetracker.views.purchase;

import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import financetracker.datatypes.Purchase;
import financetracker.models.UneditablePurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.windowing.customtables.PurchasedItemsTable;

/**
 * FrameView where the user can see details about their selected purchase
 */
public class DetailedPurchaseView extends FrameView {
    private UneditablePurchasedItemTableModel pitm;

    public DetailedPurchaseView(Purchase purchase) {
        this.pitm = new UneditablePurchasedItemTableModel(purchase.getBoughtItemsUnmodifiable());

        setTitle("Purchase " + purchase.getId());

        initCompnents();    

        pack();
        setLocationRelativeTo(null);
    }


    private void initCompnents() {
        setLayout(new BorderLayout());
        
        PurchasedItemsTable table = new PurchasedItemsTable(pitm);
        JScrollPane scorllPane = new JScrollPane(table);

        add(scorllPane, BorderLayout.CENTER);
    }
}
