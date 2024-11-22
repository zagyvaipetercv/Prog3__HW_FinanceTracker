package financetracker.views.purchase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import financetracker.controllers.PurchaseController;
import financetracker.datatypes.Purchase;
import financetracker.exceptions.purchase.DeletingPurchaseFailed;
import financetracker.models.PurchaseListModel;
import financetracker.views.base.PanelView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MyWindowConstants;
import financetracker.windowing.OptionsPanel;

public class PurchaseView extends PanelView {

    private PurchaseController purchaseController;
    private PurchaseListModel plm;

    public PurchaseView(PurchaseController purchaseController, PurchaseListModel purchaseListModel) {
        this.purchaseController = purchaseController;
        this.plm = purchaseListModel;

        initCompnents();
    }

    private void initCompnents() {
        setLayout(new BorderLayout());

        JList<Purchase> purchasesList = new JList<>(plm);

        purchasesList.setCellRenderer(new PurchaseListCellRenderer());

        JScrollPane scorllPane = new JScrollPane(purchasesList);
        add(scorllPane, BorderLayout.CENTER);

        OptionsPanel optionsPanel = new OptionsPanel();
        add(optionsPanel, BorderLayout.EAST);

        optionsPanel.addOptionButton(
                "Add new Record",
                ae -> purchaseController.getAddPurchasesView().setVisible(true));

        optionsPanel.addOptionButton(
                "Edit Selected",
                ae -> purchaseController.getEditPurchaseView(purchasesList.getSelectedValue()).setVisible(true));

        optionsPanel.addOptionButton(
                "Delete Selected",
                ae -> {
                    try {
                        purchaseController.deletePurchase(purchasesList.getSelectedValue());
                        purchaseController.refreshPurchaseView();
                    } catch (DeletingPurchaseFailed e) {
                        ErrorBox.show(this, e);
                    }
                });

    }

    private class PurchaseListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean hasFocus) {

            JPanel result = new JPanel(new GridLayout(1, 3));
            result.setPreferredSize(new Dimension(0, MyWindowConstants.LIST_CELL_PREFFERED_HEIGHT));

            if (!value.getClass().equals(Purchase.class)) {
                return null;
            }

            Purchase purchase = (Purchase) value;

            JLabel idLabel = new JLabel("ID: " + purchase.getId());
            idLabel.setHorizontalAlignment(SwingConstants.CENTER);
            result.add(idLabel);

            JLabel dateLabel = new JLabel(purchase.getDateOfPurchase().toString());
            dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
            result.add(dateLabel);

            JLabel sumPriceLabel = new JLabel(purchase.getSumPrice().toString());
            sumPriceLabel.setHorizontalAlignment(SwingConstants.CENTER);
            result.add(sumPriceLabel);

            result.setBorder(
                    BorderFactory.createLineBorder(MyWindowConstants.BORDER_COLOR, MyWindowConstants.BORDER_THICKNESS));

            if (isSelected) {
                result.setBackground(list.getSelectionBackground());
                result.setForeground(list.getSelectionForeground());
            } else {
                result.setBackground(list.getBackground());
                result.setForeground(list.getForeground());
            }

            return result;
        }
    }
}
