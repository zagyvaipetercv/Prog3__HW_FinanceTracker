package financetracker.controllers;

import java.time.LocalDate;
import java.util.List;

import financetracker.datatypes.BoughtItem;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;
import financetracker.datatypes.Purchase;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.category.CreatingCategoryFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.purchase.CreatingPurchaseFailedException;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.exceptions.purchase.InvalidTableCellException;
import financetracker.models.PurchaseListModel;
import financetracker.models.PurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.purchase.AddPurchaseView;
import financetracker.views.purchase.PurchaseView;
import financetracker.windowing.MainFrame;

public class PurchaseController extends Controller<Purchase> {

    private static final String DEFAULT_SAVE_PATH = "saves/purchases.dat";

    private PurchaseListModel purchaseListModel = new PurchaseListModel();

    public PurchaseController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public PurchaseController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);
    }

    public PanelView getPurchaseView() {
        return new PurchaseView(this, purchaseListModel);
    }

    public FrameView getAddPurchasesView() {
        return new AddPurchaseView(this);
    }

    public void deleteRow(PurchasedItemTableModel pitm, int rowIndex) throws DeleteUnfinishedEmptyRowException {
        if (rowIndex == pitm.getRowCount() - 1) {
            throw new DeleteUnfinishedEmptyRowException("Can't delete unfinished empty row");
        }

        pitm.deleteRow(rowIndex);
    }

    public void addPurchase(PurchasedItemTableModel pitm, LocalDate date) throws InvalidTableCellException, CreatingPurchaseFailedException, CategoryLookupFailedException, CreatingCategoryFailedException {
        checkCells(pitm);

        List<BoughtItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new CreatingPurchaseFailedException("List has no items", null); 
        }

        for (BoughtItem boughtItem : purchasedItems) { // Set the category for items
            Category category;
            CategoryController categoryController = mainFrame.getCategoryController();
            try { // Try adding category form existing ones
                long categoryId = categoryController.getCategoryId(boughtItem.getCategory().getName());
                category = categoryController.getCategory(categoryId);
            } catch (SerializerCannotRead e) {
                throw new CategoryLookupFailedException("Looking up existing categories failed");
            } catch (IdNotFoundException e) { // If category did not exist yet -> create new category 
                category = categoryController.createCategory(boughtItem.getCategory().getName());
            }
            boughtItem.setCategory(category);
        }

        Purchase purchase = new Purchase(modelSerializer.getNextId(), mainFrame.getUserLogedIn(), date, purchasedItems);
        try {
            modelSerializer.appendNewData(purchase);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingPurchaseFailedException("Creating purchase failed due to IO Error", purchase);
        }
    }

    private void checkCells(PurchasedItemTableModel pitm) throws InvalidTableCellException {
        for (int row = 0; row < pitm.getRowCount() - 1; row++) { // Last row is empty row -> don need to check it
            for (int column = 0; column < pitm.getColumnCount(); column++) {
                // Check if column is empty
                String text = pitm.getValueAt(row, column).toString();
                if (text.isBlank()) {
                    throw new InvalidTableCellException(row, column, text, pitm.getColumnName(column));
                }

                // Check if money is 0 or less
                if (pitm.getColumnClass(column).equals(Money.class)) {
                    Money money = (Money) pitm.getValueAt(row, column);
                    if (money.getAmount() <= 0.0) {
                        throw new InvalidTableCellException(row, column, text, pitm.getColumnName(column));
                    }
                }

                // Check if double is 0 or less
                if (pitm.getColumnClass(column).equals(Double.class)) {
                    double amount = (double) pitm.getValueAt(row, column);
                    if (amount <= 0.0) {
                        throw new InvalidTableCellException(row, column, text, pitm.getColumnName(column));
                    }
                }
            }
        }
    }
}
