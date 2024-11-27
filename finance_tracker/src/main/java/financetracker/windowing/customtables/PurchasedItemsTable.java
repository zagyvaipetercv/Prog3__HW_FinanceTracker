package financetracker.windowing.customtables;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import financetracker.models.DetailedPurchaseTableModel;
import financetracker.models.PurchasedItemTableModel;
import financetracker.windowing.celleditors.CategoryCellEditor;
import financetracker.windowing.celleditors.CustomDoubleCellEditor;
import financetracker.windowing.celleditors.CustomStringCellEditor;
import financetracker.windowing.celleditors.MoneyCellEditor;

/**
 * A custom JTable for PurchasedItem types
 * <p>
 * Cell editors are set to work
 */
public class PurchasedItemsTable extends JTable {
    public PurchasedItemsTable(PurchasedItemTableModel pitm) {
        super(pitm);
        getColumnModel().getColumn(0).setCellEditor(new CategoryCellEditor(this));
        getColumnModel().getColumn(1).setCellEditor(new CustomStringCellEditor());
        getColumnModel().getColumn(2).setCellEditor(new MoneyCellEditor(this));
        getColumnModel().getColumn(3).setCellEditor(new CustomDoubleCellEditor(this));
        getColumnModel().getColumn(4).setCellEditor(new MoneyCellEditor(this));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public PurchasedItemsTable(DetailedPurchaseTableModel detailedPurchaseTableModel) {
        super(detailedPurchaseTableModel);
    }
}