package financetracker.views.purchase;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;

import financetracker.controllers.PurchaseController;
import financetracker.datatypes.Purchase;
import financetracker.models.PurchaseListModel;
import financetracker.views.base.PanelView;

public class PurchaseView extends PanelView {

    private PurchaseController purchaseController;

    public PurchaseView(PurchaseController purchaseController, PurchaseListModel plm) {
        this.purchaseController = purchaseController;


        JList<Purchase> purchasesList = new JList<>(plm);
    }

    private class PurchaseListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean hasFocus) {

            JPanel result = new JPanel();

            return result;
        }
    }
}
