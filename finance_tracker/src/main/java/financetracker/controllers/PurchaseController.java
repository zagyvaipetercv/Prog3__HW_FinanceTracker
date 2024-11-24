package financetracker.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.datatypes.PurchasedItem;
import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;
import financetracker.datatypes.Purchase;
import financetracker.datatypes.User;
import financetracker.exceptions.ChangingViewFailed;
import financetracker.exceptions.FilteringFailed;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotChangeException;
import financetracker.exceptions.cashflowcontroller.DeletingCashFlowFailed;
import financetracker.exceptions.cashflowcontroller.EditingCashFlowFailed;
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
import financetracker.exceptions.purchase.UpadtingPurchaseModelFailed;
import financetracker.exceptions.purchase.UpdatingPurchasedItemModelFailed;
import financetracker.models.DetailedPurchaseTableModel;
import financetracker.models.PurchaseListModel;
import financetracker.models.PurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.purchase.AddPurchaseView;
import financetracker.views.purchase.DetailedPurchaseView;
import financetracker.views.purchase.EditPurchaseView;
import financetracker.views.purchase.PurchaseView;
import financetracker.views.purchase.PurchasedItemsView;
import financetracker.windowing.MainFrame;

public class PurchaseController extends Controller<Purchase> {

    private static final String DEFAULT_SAVE_PATH = "saves/purchases.dat";

    private CashFlowController cashFlowController;
    private CategoryController categoryController;

    private PurchaseListModel purchaseListModel;
    private DetailedPurchaseTableModel purchasedItemsModel;

    private User userLogedIn;

    // FILTER OPTIONS
    private String filterID;

    private LocalDate startDate;
    private LocalDate endDate;
    private Category category;

    public PurchaseController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public PurchaseController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);
        userLogedIn = mainFrame.getUserLogedIn();
        cashFlowController = mainFrame.getCashFlowController();
        categoryController = mainFrame.getCategoryController();

        LocalDate today = LocalDate.now();
        int daysInThisMonth = YearMonth.of(today.getYear(), today.getMonthValue()).lengthOfMonth();

        // Setting up filter variables
        startDate = LocalDate.of(today.getYear(), today.getMonth(), 1);
        endDate = LocalDate.of(today.getYear(), today.getMonth(), daysInThisMonth);
        category = null;

        try {
            List<Purchase> usersPurchases = getPurchases();
            purchaseListModel = new PurchaseListModel(usersPurchases);
            purchasedItemsModel = new DetailedPurchaseTableModel();
            updatePurchasedItemModel(startDate, endDate);
        } catch (ReadingPurchasesFailedException e) {
            throw new ControllerWasNotCreated("Purchases could not be initalized", PurchaseController.class);
        } catch (UpdatingPurchasedItemModelFailed e) {
            throw new ControllerWasNotCreated("Purchased Itmes could not be initalized", PurchaseController.class);
        }

    }

    public PanelView getPurchaseView() {
        return new PurchaseView(this, purchaseListModel, filterID);
    }

    public PanelView getPurchsedItemsView() throws ChangingViewFailed {
        try {
            return new PurchasedItemsView(this,
                    purchasedItemsModel,
                    startDate, endDate, category,
                    categoryController.getCategoriesNames());
        } catch (CategoryLookupFailedException e) {
            throw new ChangingViewFailed("Changing to Purchased Items Failed");
        }
    }

    public FrameView getAddPurchasesView() {
        return new AddPurchaseView(this);
    }

    public FrameView getEditPurchaseView(Purchase purchase) {
        return new EditPurchaseView(this, purchase);
    }

    public FrameView getDetailedPurcahseView(Purchase purchase) {
        return new DetailedPurchaseView(purchase);
    }

    public void refreshPurchaseView() {
        mainFrame.changeView(getPurchaseView());
    }

    public void refreshPurchasedItemsView() throws ChangingViewFailed {
        mainFrame.changeView(getPurchsedItemsView());
    }

    public void deleteRow(PurchasedItemTableModel pitm, int rowIndex) throws DeleteUnfinishedEmptyRowException {
        if (rowIndex == pitm.getRowCount() - 1) {
            throw new DeleteUnfinishedEmptyRowException("Can't delete unfinished empty row");
        }

        pitm.deleteRow(rowIndex);
    }

    public void addPurchase(PurchasedItemTableModel pitm, LocalDate date) throws InvalidTableCellException,
            CreatingPurchaseFailedException, CategoryLookupFailedException, CreatingCategoryFailedException,
            UpadtingPurchaseModelFailed {
        checkCells(pitm);

        List<PurchasedItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new CreatingPurchaseFailedException("List has no items", null);
        }

        lookupCategories(purchasedItems);

        double sumPrice = 0.0;
        for (PurchasedItem purchasedItem : purchasedItems) {
            sumPrice += purchasedItem.getSumPrice().getAmount();
        }

        Purchase purchase = null;
        try {
            long purchaseId = modelSerializer.getNextId();
            CashFlow cashFlow = cashFlowController.addNewCashFlow(userLogedIn, date, sumPrice,
                    Currency.getInstance("HUF"), "Purchase: " + purchaseId);

            purchase = new Purchase(purchaseId, mainFrame.getUserLogedIn(), date, purchasedItems,
                    cashFlow);

            for (PurchasedItem purchasedItem : purchasedItems) {
                purchasedItem.setPurchase(purchase);
            }

            modelSerializer.appendNewData(purchase);
        } catch (InvalidReasonException | BalanceCouldNotChangeException e) {
            throw new CreatingPurchaseFailedException("Cashflow for purcahse could not register", null);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingPurchaseFailedException("Creating purchase failed due to IO Error", purchase);
        }
        updatePurchaseModel();
    }

    public void editPurchase(Purchase purchase, PurchasedItemTableModel pitm, LocalDate dateOfPurchase)
            throws InvalidTableCellException, CategoryLookupFailedException, CreatingCategoryFailedException,
            EditingPurchaseFailed, UpadtingPurchaseModelFailed {
        checkCells(pitm);
        List<PurchasedItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new EditingPurchaseFailed("Purchase has no items", purchase);
        }

        lookupCategories(purchasedItems);

        purchase.setBoughtItems(pitm.getItems());
        purchase.setDateOfPurchase(dateOfPurchase);

        for (PurchasedItem item : purchase.getBoughtItems()) {
            item.setPurchase(purchase);
        }

        try {
            cashFlowController.editCashFlow(purchase.getCashFlow(), purchase.getSumPrice(),
                    purchase.getCashFlow().getReason(), dateOfPurchase);
        } catch (EditingCashFlowFailed e) {
            throw new EditingPurchaseFailed("Editing purchse failed due to cahsflow edit failiure", purchase);
        }

        try {
            modelSerializer.changeData(purchase);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingPurchaseFailed("Editing purchase failed due to an IO Error", purchase);
        }

        updatePurchaseModel();
    }

    private void lookupCategories(List<PurchasedItem> purchasedItems)
            throws CategoryLookupFailedException, CreatingCategoryFailedException {
        for (PurchasedItem boughtItem : purchasedItems) { // Set the category for items
            Category categoryForItem;
            try { // Try adding category form existing ones
                long categoryId = categoryController.getCategoryId(boughtItem.getCategory().getName());
                categoryForItem = categoryController.getCategory(categoryId);
            } catch (IdNotFoundException e) { // If category did not exist yet -> create new category
                categoryForItem = categoryController.createCategory(boughtItem.getCategory().getName());
            }
            boughtItem.setCategory(categoryForItem);
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

    private void updatePurchaseModel(long id) throws UpadtingPurchaseModelFailed {
        try {
            List<Purchase> purchases = getPurchases();
            List<Purchase> result = new ArrayList<>();

            for (Purchase purchase : purchases) {
                if (purchase.getId() == id) {
                    result.add(purchase);
                }
            }
            purchaseListModel = new PurchaseListModel(result);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpadtingPurchaseModelFailed("Updating purchase model failed due to an IO Error");
        }
    }

    private void updatePurchaseModel() throws UpadtingPurchaseModelFailed {
        try {
            List<Purchase> purchases = getPurchases();
            purchaseListModel = new PurchaseListModel(purchases);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpadtingPurchaseModelFailed("Updating purchase model failed due to an IO Error");
        }
    }

    private void updatePurchasedItemModel(LocalDate startDate, LocalDate endDate, Category category)
            throws UpdatingPurchasedItemModelFailed {
        try {
            List<Purchase> purchases = getPurchases();
            List<PurchasedItem> purchasedItems = new ArrayList<>();
            for (Purchase purchase : purchases) {
                if (purchase.getDateOfPurchase().isBefore(startDate) || purchase.getDateOfPurchase().isAfter(endDate)) {
                    continue; // Skip purchases that are not betweeen start and end dates
                }

                for (PurchasedItem purchasedItem : purchase.getBoughtItems()) {
                    if (category != null && !category.equals(purchasedItem.getCategory())) {
                        continue; // Skip items where category does not match
                    }

                    purchasedItems.add(purchasedItem);
                }
            }

            purchasedItemsModel = new DetailedPurchaseTableModel(purchasedItems);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpdatingPurchasedItemModelFailed("Updating purchased item model faield due to an IO Error");
        }
    }

    private void updatePurchasedItemModel(LocalDate startDate, LocalDate endDate)
            throws UpdatingPurchasedItemModelFailed {
        updatePurchasedItemModel(startDate, endDate, null);
    }

    public void deletePurchase(Purchase purchase) throws DeletingPurchaseFailed, UpadtingPurchaseModelFailed {
        try {
            cashFlowController.deleteCashFlow(purchase.getCashFlow());
            modelSerializer.removeData(purchase.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingPurchaseFailed("Deleting purchase failed due to an IO Error", purchase);
        } catch (DeletingCashFlowFailed e) {
            throw new DeletingPurchaseFailed("Deleting purcahse failed due to CashFlow error", purchase);
        }

        updatePurchaseModel();
    }

    public void filterPurchasedItems(LocalDate startDate, LocalDate endDate, String categoryString)
            throws FilteringFailed, UpdatingPurchasedItemModelFailed {
        // Get the category
        Category cat = null;
        if (!categoryString.isBlank()) { // Only change value of cat if its not blank
            try {
                cat = categoryController.getCategory(categoryString);
            } catch (CategoryLookupFailedException e) {
                throw new FilteringFailed("Category was not found");
            }
        }

        // Update the table model
        updatePurchasedItemModel(startDate, endDate, cat);

        // Update filtering uptions
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = cat;
    }

    public void filterPurchase(String idString) throws FilteringFailed, UpadtingPurchaseModelFailed {
        if (idString.isBlank()) {
            updatePurchaseModel();
            filterID = "";
            return;
        }

        try {
            long id = Long.parseLong(idString);
            this.filterID = idString;
            updatePurchaseModel(id);
        } catch (NumberFormatException e) {
            throw new FilteringFailed(idString + " can't be parsed to id [whole number]");
        }
    }
}
