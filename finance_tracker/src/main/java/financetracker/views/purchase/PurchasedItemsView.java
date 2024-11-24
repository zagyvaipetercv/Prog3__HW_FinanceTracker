package financetracker.views.purchase;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import financetracker.datatypes.BoughtItem;
import financetracker.models.UneditablePurchasedItemTableModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.customtables.PurchasedItemsTable;

public class PurchasedItemsView extends PanelView {
    private UneditablePurchasedItemTableModel upitm;

    public PurchasedItemsView(List<BoughtItem> items) {
        upitm = new UneditablePurchasedItemTableModel(items);


        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        PurchasedItemsTable table = new PurchasedItemsTable(upitm);
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
    }

    private class FilterPanel extends JPanel {
    
    }
}

