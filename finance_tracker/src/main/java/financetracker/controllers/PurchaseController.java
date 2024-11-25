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
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.generic.ChangingViewFailed;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.generic.DeletingRecordFailed;
import financetracker.exceptions.generic.EditingRecordFailed;
import financetracker.exceptions.generic.FilteringFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.exceptions.models.NoItemWasSelected;
import financetracker.exceptions.modelserailizer.IdNotFoundException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.exceptions.purchase.InvalidTableCellException;
import financetracker.exceptions.purchase.ReadingPurchasesFailedException;
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

    // USED CONTROLLERS
    private CashFlowController cashFlowController;
    private CategoryController categoryController;

    // MODELS
    private PurchaseListModel purchaseListModel;
    private DetailedPurchaseTableModel purchasedItemsTableModel;

    // USER
    private User userLogedIn;

    // FILTER OPTIONS
    private boolean filterForID;
    private long filterID;

    private LocalDate startDate;
    private LocalDate endDate;
    private Category category;

    // CONSTRUCTORS
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

        filterForID = false;
        filterID = 0;
    }

    // VIEW GETTERS
    public PanelView getPurchaseView() throws UpdatingModelFailed {
        if (filterForID) {
            updatePurchaseModel(filterID);
        } else {
            updatePurchaseModel();
        }
        return new PurchaseView(this, purchaseListModel, (!filterForID ? "" : Long.toString(filterID)));
    }

    public PanelView getPurchsedItemsView() throws UpdatingModelFailed, ChangingViewFailed {
        updatePurchasedItemModel(startDate, endDate, category);

        try {
            return new PurchasedItemsView(this,
                    purchasedItemsTableModel,
                    startDate, endDate, category,
                    categoryController.getCategoryNames());
        } catch (CategoryLookupFailedException e) {
            throw new ChangingViewFailed("Changing to Purchased Items Failed");
        }
    }

    public FrameView getAddPurchasesView() {
        return new AddPurchaseView(this);
    }

    public FrameView getEditPurchaseView(Purchase purchase) throws NoItemWasSelected {
        if (purchase == null) {
            throw new NoItemWasSelected("No purchase was selected to edit");
        }

        return new EditPurchaseView(this, purchase);
    }

    public FrameView getDetailedPurcahseView(Purchase purchase) throws NoItemWasSelected {
        if (purchase == null) {
            throw new NoItemWasSelected("No purchase was selected for details");
        }
        return new DetailedPurchaseView(purchase);
    }

    public void refreshPurchaseView() throws UpdatingModelFailed {
        mainFrame.changeView(getPurchaseView());
    }

    public void refreshPurchasedItemsView() throws ChangingViewFailed, UpdatingModelFailed {
        mainFrame.changeView(getPurchsedItemsView());
    }

    // ACTIONS
    public void deleteRow(PurchasedItemTableModel pitm, int rowIndex) throws DeleteUnfinishedEmptyRowException {
        if (rowIndex == pitm.getRowCount() - 1) {
            throw new DeleteUnfinishedEmptyRowException("Can't delete unfinished empty row");
        }

        pitm.deleteRow(rowIndex);
    }

    public void addPurchase(PurchasedItemTableModel pitm, LocalDate date) throws InvalidTableCellException,
            CreatingRecordFailed, CategoryLookupFailedException {
        checkCells(pitm);

        List<PurchasedItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new CreatingRecordFailed("Creating purchase failed - There is no purchased item in the list", null);
        }

        try {
            lookupCategories(purchasedItems);
        } catch (CategoryLookupFailedException | CreatingRecordFailed e) {
            throw new CreatingRecordFailed("Creating purchase faield due to categroy lookup Error", null);
        }

        double sumPrice = 0.0;
        for (PurchasedItem purchasedItem : purchasedItems) {
            sumPrice += purchasedItem.getSumPrice().getAmount();
        }

        Purchase purchase = null;
        try {
            long purchaseId = modelSerializer.getNextId();
            CashFlow cashFlow = cashFlowController.addNewCashFlow(userLogedIn, date, -sumPrice,
                    Currency.getInstance("HUF"), "Purchase: " + purchaseId);

            purchase = new Purchase(purchaseId, mainFrame.getUserLogedIn(), date, purchasedItems,
                    cashFlow);

            for (PurchasedItem purchasedItem : purchasedItems) {
                purchasedItem.setPurchase(purchase);
            }

            modelSerializer.appendNewData(purchase);
        } catch (InvalidReasonException | CreatingRecordFailed e) {
            throw new CreatingRecordFailed("Creating purchase failed - Cashflow for purchase could not register",
                    purchase);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingRecordFailed("Creating purchase failed due to IO Error", purchase);
        }
    }

    public void editPurchase(Purchase purchase, PurchasedItemTableModel pitm, LocalDate dateOfPurchase)
            throws InvalidTableCellException, EditingRecordFailed {

        if (purchase == null) {
            throw new EditingRecordFailed("Purchase is null", null);
        }

        checkCells(pitm);
        List<PurchasedItem> purchasedItems = pitm.getItems();

        if (purchasedItems.isEmpty()) {
            throw new EditingRecordFailed("Edting purchase failed - Purchase has no items", purchase);
        }

        try {
            lookupCategories(purchasedItems);
        } catch (CategoryLookupFailedException | CreatingRecordFailed e) {
            throw new EditingRecordFailed("Editing purchase faield due to categroy lookup Error", purchase);
        }

        purchase.setBoughtItems(pitm.getItems());
        purchase.setDateOfPurchase(dateOfPurchase);

        for (PurchasedItem item : purchase.getBoughtItems()) {
            item.setPurchase(purchase);
        }

        Money sumPrice = new Money(-purchase.getSumPrice().getAmount(), Currency.getInstance("HUF"));

        try {
            cashFlowController.editCashFlow(purchase.getCashFlow(), sumPrice,
                    purchase.getCashFlow().getReason(), dateOfPurchase);
        } catch (EditingRecordFailed e) {
            throw new EditingRecordFailed("Editing purchse failed due to cahsflow edit failiure", purchase);
        }

        try {
            modelSerializer.changeData(purchase);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingRecordFailed("Editing purchase failed due to an IO Error", purchase);
        }
    }

    public void deletePurchase(Purchase purchase) throws DeletingRecordFailed, NoItemWasSelected {
        if (purchase == null) {
            throw new NoItemWasSelected("A purchase must be selected to delete");
        }

        try {
            cashFlowController.deleteCashFlow(purchase.getCashFlow());
            modelSerializer.removeData(purchase.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingRecordFailed("Deleting purchase failed due to an IO Error", purchase);
        } catch (DeletingRecordFailed e) {
            throw new DeletingRecordFailed("Deleting purcahse failed due to CashFlow error", purchase);
        }
    }

    // HELPERS
    private void lookupCategories(List<PurchasedItem> purchasedItems)
            throws CategoryLookupFailedException, CreatingRecordFailed {
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

    private List<Purchase> getUsersPurchases() throws ReadingPurchasesFailedException {
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

    // VALIDATORS
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

    // MODEL UPDATE
    private void updatePurchaseModel(long id) throws UpdatingModelFailed {
        try {
            List<Purchase> purchases = getUsersPurchases();
            List<Purchase> result = new ArrayList<>();

            for (Purchase purchase : purchases) {
                if (purchase.getId() == id) {
                    result.add(purchase);
                }
            }
            purchaseListModel = new PurchaseListModel(result);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpdatingModelFailed("Updating purchase model failed due to an IO Error");
        }
    }

    private void updatePurchaseModel() throws UpdatingModelFailed {
        try {
            List<Purchase> purchases = getUsersPurchases();
            purchaseListModel = new PurchaseListModel(purchases);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpdatingModelFailed("Updating purchase model failed due to an IO Error");
        }
    }

    private void updatePurchasedItemModel(LocalDate startDate, LocalDate endDate, Category category)
            throws UpdatingModelFailed {
        try {
            List<Purchase> purchases = getUsersPurchases();
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

            purchasedItemsTableModel = new DetailedPurchaseTableModel(purchasedItems);
        } catch (ReadingPurchasesFailedException e) {
            throw new UpdatingModelFailed("Updating purchased item model faield due to an IO Error");
        }
    }

    // FILTERING
    public void filterPurchasedItems(LocalDate startDate, LocalDate endDate, String categoryString)
            throws FilteringFailed, UpdatingModelFailed {
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

    public void filterPurchase(String idString) throws FilteringFailed, UpdatingModelFailed {
        if (idString.isBlank()) {
            updatePurchaseModel();
            filterForID = false;
            return;
        }

        try {
            filterForID = true;
            filterID = Long.parseLong(idString);
            updatePurchaseModel(filterID);
        } catch (NumberFormatException e) {
            throw new FilteringFailed(idString + " can't be parsed to id [whole number]");
        }
    }

    // FOR-TESTING
    public List<Purchase> getAllPurchases() throws SerializerCannotRead {
        return modelSerializer.readAll();
    }
}
