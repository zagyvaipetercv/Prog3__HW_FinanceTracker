package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.datatypes.BoughtItem;
import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;
import financetracker.datatypes.Purchase;
import financetracker.datatypes.User;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.DeletingCashFlowFailed;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.category.CreatingCategoryFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.purchase.CreatingPurchaseFailedException;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.exceptions.purchase.DeletingPurchaseFailed;
import financetracker.exceptions.purchase.EditingPurchaseFailed;
import financetracker.exceptions.purchase.InvalidTableCellException;
import financetracker.exceptions.purchase.ReadingPurchasesFailedException;
import financetracker.models.PurchaseListModel;
import financetracker.models.PurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.purchase.AddPurchaseView;
import financetracker.views.purchase.EditPurchaseView;
import financetracker.views.purchase.PurchaseView;
import financetracker.windowing.MainFrame;

public class PurchaseController extends Controller<Purchase> {

    private static final String DEFAULT_SAVE_PATH = "saves/purchases.dat";

    private PurchaseListModel purchaseListModel;
    private User userLogedIn;

    private CashFlowController cashFlowController;

    public PurchaseController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public PurchaseController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);
        userLogedIn = mainFrame.getUserLogedIn();
        cashFlowController = mainFrame.getCashFlowController();

        try {
            List<Purchase> usersPurchases = getPurchases();
            purchaseListModel = new PurchaseListModel(usersPurchases);
        } catch (ReadingPurchasesFailedException e) {
            throw new ControllerWasNotCreated("Purchases could not be initalized", PurchaseController.class);
        }

    }

    public PanelView getPurchaseView() {
        return new PurchaseView(this, purchaseListModel);
    }

    public FrameView getAddPurchasesView() {
        return new AddPurchaseView(this);
    }

    public FrameView getEditPurchaseView(Purchase purchase) {
        return new EditPurchaseView(this, purchase);
    }

    public void refreshPurchaseView() {
        mainFrame.changeView(getPurchaseView());
    }

    public void deleteRow(PurchasedItemTableModel pitm, int rowIndex) throws DeleteUnfinishedEmptyRowException {
        if (rowIndex == pitm.getRowCount() - 1) {
            throw new DeleteUnfinishedEmptyRowException("Can't delete unfinished empty row");
        }

        pitm.deleteRow(rowIndex);
    }

    public void addPurchase(PurchasedItemTableModel pitm, LocalDate date) throws InvalidTableCellException,
            CreatingPurchaseFailedException, CategoryLookupFailedException, CreatingCategoryFailedException {
        checkCells(pitm);

        List<BoughtItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new CreatingPurchaseFailedException("List has no items", null);
        }

        lookupCategories(purchasedItems);

        double sumPrice = 0.0;
        for (BoughtItem boughtItem : purchasedItems) {
            sumPrice += boughtItem.getSumPrice().getAmount();
        }

        Purchase purchase = null;
        try {
            long purchaseId = modelSerializer.getNextId();
            CashFlow cashFlow = cashFlowController.addNewCashFlow(userLogedIn, date, sumPrice,
                    Currency.getInstance("HUF"), "Purchase: " + purchaseId);

            purchase = new Purchase(purchaseId, mainFrame.getUserLogedIn(), date, purchasedItems,
                    cashFlow);

            modelSerializer.appendNewData(purchase);
        } catch (InvalidReasonException | BalanceCouldNotCahcngeException e) {
            throw new CreatingPurchaseFailedException("Cashflow for purcahse could not register", null);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingPurchaseFailedException("Creating purchase failed due to IO Error", purchase);
        }
        upadteModel();
    }

    public void editPurchase(Purchase purchase, PurchasedItemTableModel pitm, LocalDate dateOfPurchase)
            throws InvalidTableCellException, CategoryLookupFailedException, CreatingCategoryFailedException,
            EditingPurchaseFailed {
        checkCells(pitm);
        List<BoughtItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new EditingPurchaseFailed("Purchase has no items", purchase);
        }

        lookupCategories(purchasedItems);

        purchase.setBoughtItems(pitm.getItems());
        purchase.setDateOfPurchase(dateOfPurchase);

        try {
            modelSerializer.changeData(purchase);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingPurchaseFailed("Editing purchase failed due to an IO Error", purchase);
        }

        try {
            cashFlowController.deleteCashFlow(purchase.getCashFlow());
            cashFlowController.addNewCashFlow(userLogedIn, dateOfPurchase, purchase.getSumPrice().getAmount(),
                    purchase.getSumPrice().getCurrency(), "Purchase: " + purchase.getId());
        } catch (DeletingCashFlowFailed | InvalidReasonException | BalanceCouldNotCahcngeException e) {
            throw new EditingPurchaseFailed("Editing purchase failed due to CashFlow Error", purchase);
        }

        upadteModel();
    }

    private void lookupCategories(List<BoughtItem> purchasedItems)
            throws CategoryLookupFailedException, CreatingCategoryFailedException {
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

    private List<Purchase> getPurchases() throws ReadingPurchasesFailedException {
        List<Purchase> result = new ArrayList<>();

        try {
            List<Purchase> purchases = modelSerializer.readAll();
            for (Purchase purchase : purchases) {
                if (purchase.getUser().equals(userLogedIn)) {
                    result.add(purchase);
                }
            }

            return result;
        } catch (SerializerCannotRead e) {
            throw new ReadingPurchasesFailedException("Failed to read purchases");
        }
    }

    private void upadteModel() {
        try {
            List<Purchase> purchases = modelSerializer.readAll();
            purchaseListModel = new PurchaseListModel(purchases);
        } catch (SerializerCannotRead e) {
        }
    }

    public void deletePurchase(Purchase purchase) throws DeletingPurchaseFailed {
        try {
            cashFlowController.deleteCashFlow(purchase.getCashFlow());
            modelSerializer.removeData(purchase.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingPurchaseFailed("Deleting purchase failed due to an IO Error", purchase);
        } catch (DeletingCashFlowFailed e) {
            throw new DeletingPurchaseFailed("Deleting purcahse failed due to CashFlow error", purchase);
        }

        upadteModel();
    }

}
