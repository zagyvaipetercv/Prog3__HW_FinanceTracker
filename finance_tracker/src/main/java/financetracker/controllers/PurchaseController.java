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
    private Long filterID;

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

        filterID = null;
    }

    // VIEW GETTERS
    /**
     * Returns an updated, filtered PurchaseView that contains information of the
     * purchases of the user signed in
     * 
     * @return an updated filtered PurchaseView
     * @throws UpdatingModelFailed if updating the PurchaseListModel model failed
     */
    public PanelView getPurchaseView() throws UpdatingModelFailed {
        updatePurchaseModel(filterID);
        return new PurchaseView(this, purchaseListModel, (filterID == null ? "" : Long.toString(filterID)));
    }

    /**
     * Returns an updated, filtered PurchasedItemsView which contains information of
     * the signed in user's purchased items
     * 
     * @return an updated, filtered PurchasedItemsView
     * @throws UpdatingModelFailed if the DetailedPurchaseTableModel failed to
     *                             update
     * @throws ChangingViewFailed  if the categories for the filtering view failed
     *                             to load
     */
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

    /**
     * Returns an AddPurchaseView where the user can add and save a new purchase
     * record
     * 
     * @return an empty AddPurchaseView
     */
    public FrameView getAddPurchasesView() {
        return new AddPurchaseView(this);
    }

    /**
     * Returns an EditPurchaseView where the user can change, add or remove
     * PurchasedItems in an existing purchase
     * 
     * @param purchase the purchase that will be changed
     * @return an EditPurchaseView filled with the existing items in it
     * @throws NoItemWasSelected if no purchase was selected
     */
    public FrameView getEditPurchaseView(Purchase purchase) throws NoItemWasSelected {
        if (purchase == null) {
            throw new NoItemWasSelected("No purchase was selected to edit");
        }

        return new EditPurchaseView(this, purchase);
    }

    /**
     * Returns a DetailedPurchaseView where the user can check details for the
     * selected purchase
     * <p>
     * The cells of the purchase can't be modified
     * 
     * @param purchase the purchase that will be detailed
     * @return a DetailedPurchaseView filled with the purchased items of the
     *         purchase
     * @throws NoItemWasSelected
     */
    public FrameView getDetailedPurcahseView(Purchase purchase) throws NoItemWasSelected {
        if (purchase == null) {
            throw new NoItemWasSelected("No purchase was selected for details");
        }
        return new DetailedPurchaseView(purchase);
    }

    /**
     * Refreshes the purchase view by creating a new instance and updating the main
     * panel of mainFrame
     * 
     * @throws UpdatingModelFailed if updating the PurchaseListModel model failed
     */
    public void refreshPurchaseView() throws UpdatingModelFailed {
        mainFrame.changeView(getPurchaseView());
    }

    /**
     * Refreshes the debt view by creating a new instance and updating the main
     * panel of mainFrame
     * 
     * @throws UpdatingModelFailed if the DetailedPurchaseTableModel failed to
     *                             update
     * @throws ChangingViewFailed  if the categories for the filtering view failed
     *                             to load
     */
    public void refreshPurchasedItemsView() throws ChangingViewFailed, UpdatingModelFailed {
        mainFrame.changeView(getPurchsedItemsView());
    }

    // ACTIONS
    /**
     * Deletes a row in the PurchasedItemTableModel
     * <p>
     * The row can't be the last unfinished/empty row
     * 
     * @param pitm     the PurchasedItemTableModel where the row will be deleted
     * @param rowIndex the index of the row that will be deleted
     * @throws DeleteUnfinishedEmptyRowException if the user tries to delete the
     *                                           lest row
     */
    public void deleteRow(PurchasedItemTableModel pitm, int rowIndex) throws DeleteUnfinishedEmptyRowException {
        if (rowIndex == pitm.getRowCount() - 1) {
            throw new DeleteUnfinishedEmptyRowException("Can't delete unfinished empty row");
        }

        pitm.deleteRow(rowIndex);
    }

    /**
     * Adds and saves a purchase with the purchased items specified in the model and
     * the date
     * <p>
     * A Cashflow with the sumPayed amount will be also created
     * 
     * @param pitm the PurchasedItemTableModel that stores the PurchasedItems of the
     *             purchase
     * @param date the date of the purchase
     * @throws InvalidTableCellException if one of the cells has an invalid value.
     *                                   <p>
     *                                   Cell can be invalid if:
     *                                   <ul>
     *                                   <li>the cell is blank</li>
     *                                   <li>the cell stores a Money value and it's
     *                                   amount is 0 or less</li>
     *                                   <li>the cell stores a double value and it's
     *                                   value is 0 or less</li>
     *                                   </ul>
     * @throws CreatingRecordFailed
     *                                   <ul>
     *                                   <li>if the model had no purchased items in
     *                                   it</li>
     *                                   <li>if looking up the category of a
     *                                   PurchsedItem faield</li>
     *                                   <li>if an IO Error occured</li>
     *                                   </ul>
     */
    public void addPurchase(PurchasedItemTableModel pitm, LocalDate date) throws InvalidTableCellException,
            CreatingRecordFailed {

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

    /**
     * Edits the an existing purchase based on the PurchasedItemTableModel and the
     * dateOfPurchase
     * <p>
     * Also edits the releveant cashflow record
     * 
     * @param purchase       the pruchase that will be changed
     * @param pitm           PurchasedItemTableModel that stores the new
     *                       PurchasedItems
     * @param dateOfPurchase the new date of the purchase
     * @throws InvalidTableCellException if one of the cells has an invalid value.
     *                                   <p>
     *                                   Cell can be invalid if:
     *                                   <ul>
     *                                   <li>the cell is blank</li>
     *                                   <li>the cell stores a Money value and it's
     *                                   amount is 0 or less</li>
     *                                   <li>the cell stores a double value and it's
     *                                   value is 0 or less</li>
     *                                   </ul>
     * @throws EditingRecordFailed
     *                                   <ul>
     *                                   <li>if the model had no purchased items in
     *                                   it</li>
     *                                   <li>if looking up the category of a
     *                                   PurchsedItem faield</li>
     *                                   <li>if an IO Error occured</li>
     *                                   </ul>
     */
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

    /**
     * Removes a purchase from the save file
     * 
     * @param purchase the purchase that will be removed
     * @throws DeletingRecordFailed if an IO Error occured or the relevant cashlfow
     *                              was not removed
     * @throws NoItemWasSelected    if no purchase was selected (purchase == null)
     */
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

    /**
     * Finds and sets the Category of the PurchasedItems given in the parameter
     * <p>
     * If a Category does not exist it will be created and assigned
     * 
     * @param purchasedItems list of purchased items
     * @throws CategoryLookupFailedException if the category was not found
     * @throws CreatingRecordFailed          if creating new category failed
     */
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

    /**
     * Returns the purchses that belong to the user signed in
     * 
     * @return the purchses that belong to the user signed in
     * @throws SerializerCannotRead if an IO Error occured
     */
    private List<Purchase> getUsersPurchases() throws SerializerCannotRead {
        List<Purchase> result = new ArrayList<>();

        List<Purchase> purchases = modelSerializer.readAll();
        for (Purchase purchase : purchases) {
            if (purchase.getUser().equals(userLogedIn)) {
                result.add(purchase);
            }
        }

        return result;
    }

    // VALIDATORS
    /**
     * Validates the cells of a PurchasedItemTableModel
     * <p>
     * Cell can be invalid if:
     * <ul>
     * <li>the cell is blank</li>
     * <li>the cell stores a Money value and it's
     * amount is 0 or less</li>
     * <li>the cell stores a double value and it's
     * value is 0 or less</li>
     * </ul>
     * 
     * If a cell was invalid an exception will be thrown
     * 
     * @param pitm the PurchasedItemTableModel
     * @throws InvalidTableCellException if the cell is invalid
     */
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

                // Check if amount is 0 or less
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
    /**
     * Updates the PurchaseListModel based on an id
     * <p>
     * The model will conatin the purchase that's id matches with the id in the
     * parameters or if the id was null every record will be part of the model
     * 
     * @param id id of the purchase
     * @throws UpdatingModelFailed if an IO Error occured
     */
    private void updatePurchaseModel(Long id) throws UpdatingModelFailed {
        try {
            List<Purchase> purchases = getUsersPurchases();
            if (id == null) { // If the id was null-> every record should be part of the model
                purchaseListModel = new PurchaseListModel(purchases);
                return;
            }

            // If id was not null -> find the purchase with the id
            List<Purchase> result = new ArrayList<>();
            for (Purchase purchase : purchases) {
                if (purchase.getId() == id) {
                    result.add(purchase);
                }
            }
            purchaseListModel = new PurchaseListModel(result);
        } catch (SerializerCannotRead e) {
            throw new UpdatingModelFailed("Updating purchase model failed due to an IO Error");
        }
    }

    /**
     * Updates the DetailedPurchaseTableModel to match the filter options.
     * <p>
     * The model will contain only those purchased items that were bought betweeen
     * the start and the end date and has the same category as the parameter
     * <p>
     * If the category parameter is set to null then modelsill not be filtered for
     * any category
     * 
     * @param startDate model will only have items that were purchased on the start
     *                  date or after the start date
     * @param endDate   model will only have items that were purchased on the start
     *                  end or before the end date
     * @param category  model will only have items that have the same category
     * @throws UpdatingModelFailed if an IO Error occured
     */
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
        } catch (SerializerCannotRead e) {
            throw new UpdatingModelFailed("Updating purchased item model faield due to an IO Error");
        }
    }

    // FILTERING

    /**
     * Updates the filter varuables and the DetailedPurchaseTableModel model
     * <p>
     * If category string is blank the model will not be fitlered for category
     * 
     * @param startDate
     * @param endDate
     * @param categoryString
     * 
     * @param startDate      model will only have items that were purchased on the
     *                       start date or after the start date
     * @param endDate        model will only have items that were purchased on the
     *                       start end or before the end date
     * @param categoryString model will only have items that have category with the
     *                       same name
     * @throws UpdatingModelFailed if model was not updated due to an IO Error
     * @throws FilteringFailed     if start date is after end date or category was
     *                             not found
     */
    public void filterPurchasedItems(LocalDate startDate, LocalDate endDate, String categoryString)
            throws FilteringFailed, UpdatingModelFailed {

        if (startDate.isAfter(endDate)) {
            throw new FilteringFailed("Start date cant be after end date");
        }

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

    /**
     * Updates the paremeters for PurchaseListModel and updates teh model
     * 
     * @param idString a string representing a purchase id the user is looking for
     *                 <p>
     *                 If id is blank the model will not fitler for any id
     * @throws FilteringFailed     if idString can't be parsed to a long value
     * @throws UpdatingModelFailed if updating the model failed due to an IO Error
     */
    public void filterPurchase(String idString) throws FilteringFailed, UpdatingModelFailed {
        if (idString.isBlank()) {
            filterID = null;
        } else {
            try {
                filterID = Long.parseLong(idString);
            } catch (NumberFormatException e) {
                throw new FilteringFailed(idString + " can't be parsed to id [whole number]");
            }
        }

        updatePurchaseModel(null);
    }

    // FOR-TESTING
    public List<Purchase> getAllPurchases() throws SerializerCannotRead {
        return modelSerializer.readAll();
    }
}
