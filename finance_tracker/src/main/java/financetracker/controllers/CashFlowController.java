package financetracker.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Money;
import financetracker.datatypes.User;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.generic.DeletingRecordFailed;
import financetracker.exceptions.generic.EditingRecordFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.models.CashFlowTableModel;
import financetracker.models.SummarizedCashFlowModel;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.cashflow.ChangeMoneyView;
import financetracker.views.cashflow.SetMoneyView;
import financetracker.views.cashflow.WalletView;
import financetracker.windowing.MainFrame;

public class CashFlowController extends Controller<CashFlow> {

    private static final String DEAFULT_SAVE_PATH = "saves\\cashflow.dat";

    // FITLERING
    private CashFlowType selectedCashFlowType;
    private int selectedYear;
    private Month selectedMonth;

    // MODELS
    private CashFlowTableModel cashFlowTableModel;
    private SummarizedCashFlowModel summarizedCashFlowModel;

    // USER
    private User userLogedIn;

    // CONSTRUCTORS
    /**
     * Creates a CashFlowController with the default save file path
     * 
     * @param mainFrame the main frame of the program
     * @throws ControllerWasNotCreated if the controller failed to initialize the
     *                                 next id or the save file
     */
    public CashFlowController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEAFULT_SAVE_PATH, mainFrame);
    }

    /**
     * Creates a CashFlowController with the specified save file path
     * 
     * @param filePath  the path of the save file
     * @param mainFrame the main frame of the program
     * @throws ControllerWasNotCreated if the controller failed to initialize the
     *                                 next id or the save file
     */
    public CashFlowController(String filePath, MainFrame mainFrame)
            throws ControllerWasNotCreated {
        super(filePath, mainFrame);

        userLogedIn = mainFrame.getUserLogedIn();

        LocalDate defaultDate = LocalDate.now();
        selectedYear = defaultDate.getYear();
        selectedMonth = defaultDate.getMonth();
        selectedCashFlowType = CashFlowType.ALL;
    }

    // VIEW GETTERS
    /**
     * Returns an updated, filtered wallet view where you can check the spendings of
     * the signed in user.
     * 
     * @return the updated version of the wallet view
     * @throws UpdatingModelFailed if model of the view could not update
     */
    public PanelView getWalletView() throws UpdatingModelFailed {
        updateModels(selectedYear, selectedMonth, selectedCashFlowType);
        return new WalletView(this,
                cashFlowTableModel, summarizedCashFlowModel,
                selectedYear, selectedMonth, selectedCashFlowType);
    }

    /**
     * Returns a ChangeMoneyView where you can add or remove money to your wallet
     * 
     * @return a ChangeWalletView
     */
    public FrameView getChangeMoneyView() {
        return new ChangeMoneyView(this);
    }

    /**
     * Returns a SetMoneyView where you can set to an exact amount of money on the
     * account
     * 
     * @return a SetMoneyView
     */
    public FrameView getSetMoneyView() {
        return new SetMoneyView(this);
    }

    // VIEW REFRESHER

    /*
     * Refreshes the wallet view by creating a new instance and updating the main
     * panel of mainFrame
     */
    public void refreshWalletView() throws UpdatingModelFailed {
        mainFrame.changeView(getWalletView());
    }

    // ACTIONS

    /**
     * Adds or removes money on the account by adding a cashflow record.
     * Automatically saves the changes to the save file
     * 
     * @param date         Date of the change
     * @param amountString amount of change (must be parsable to double)
     * @param currency     currency of change
     * @param reason       reason of the change (e.g. salary, found money on the
     *                     street, etc.)
     * @throws InvalidReasonException if reason is blank or null
     * @throws CreatingRecordFailed   if creating cashflow failed due to an IO Error
     * @throws InvalidAmountException if amount is blank or can't be parsed to a
     *                                double
     */
    public void changeMoneyOnAccount(LocalDate date, String amountString, Currency currency, String reason)
            throws InvalidReasonException, CreatingRecordFailed, InvalidAmountException {

        double amount = Money.parseAmount(amountString);
        addNewCashFlow(userLogedIn, date, amount, currency, reason);
    }

    /**
     * Adds a new CashFlow record to a user and returns the ashflow istance created.
     * 
     * @param user     the user the cashflow belongs to
     * @param date     date of the record
     * @param amount   amount of the money for the record
     * @param currency currency of the money for the record
     * @param reason   reason of the change (e.g. salary, found money on the street,
     *                 etc.)
     * @return the cashflow created
     * 
     * @throws InvalidReasonException if reason is blank or null
     * @throws CreatingRecordFailed   if creating cashflow failed due to an IO Error
     */
    public CashFlow addNewCashFlow(User user, LocalDate date, double amount, Currency currency, String reason)
            throws InvalidReasonException, CreatingRecordFailed {
        if (reasonIsInvalid(reason)) {
            throw new InvalidReasonException(reason, "Reason can't be blank");
        }

        Money money = new Money(amount, currency);
        CashFlow cashFlow = new CashFlow(
                modelSerializer.getNextId(),
                user,
                date,
                money,
                reason);

        try {
            modelSerializer.appendNewData(cashFlow); // Write data

        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingRecordFailed("Failed to create new cashflow record", cashFlow);
        }
        return cashFlow;
    }

    /**
     * Sets the money on the account to an excact amount by creating a new CashFlow
     * record with today's date.
     * 
     * @param newAmountString amount of money on the account should have after the
     *                        process
     * @param currency        new currency of money
     * @param reason          reason of change
     * @throws InvalidAmountException if the amount is blank or can't be parsed to a
     *                                double
     * @throws CreatingRecordFailed   if creating cashflow failed due to an IO Error
     * @throws InvalidReasonException if the reason is blank or null
     */
    public void setMoneyOnAccount(String newAmountString, Currency currency, String reason)
            throws InvalidAmountException, CreatingRecordFailed, InvalidReasonException {
        double newAmount = Money.parseAmount(newAmountString);
        LocalDate today = LocalDate.now();
        double currentAmount;
        try {
            currentAmount = getMoneyOnAccount().getAmount();
        } catch (SerializerCannotRead e) {
            throw new CreatingRecordFailed("Setting money on account failed due to reading current amount failiure",
                    null);
        }
        double difference = newAmount - currentAmount;
        addNewCashFlow(userLogedIn, today, difference, currency, reason);
    }

    /**
     * Removes a cashflow record from the save file.
     * 
     * @param cashFlow the cashflow that should be removed
     * @throws DeletingRecordFailed if deleting failed due to an IO Error
     */
    public void deleteCashFlow(CashFlow cashFlow) throws DeletingRecordFailed {
        try {
            modelSerializer.removeData(cashFlow.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingRecordFailed("Deleting Cashflow failed", cashFlow);
        }
    }

    /**
     * Changes the parameters of a cashflow.
     * 
     * @param cashFlow the cashflow that should be cahnged
     * @param money    the new money amount
     * @param reason   the new reason of the cashflow
     * @param date     the new date of the cashflow
     * @throws EditingRecordFailed if editing failed due to an IO Error
     */
    public void editCashFlow(CashFlow cashFlow, Money money, String reason, LocalDate date)
            throws EditingRecordFailed {
        cashFlow.setDate(date);
        cashFlow.setMoney(money);
        cashFlow.setReason(reason);

        try {
            modelSerializer.changeData(cashFlow);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingRecordFailed("Cashflow could not change due to an IO Error", cashFlow);
        }
    }

    /**
     * Reads the current amount of money on the account from the save file.
     * 
     * @return the amount of money on the save file
     * @throws SerializerCannotRead if the save file had issue while reading
     */
    private Money getMoneyOnAccount() throws SerializerCannotRead {
        double sum = 0;
        for (CashFlow cashFlow : modelSerializer.readAll()) {
            if (cashFlow.getUser().equals(userLogedIn)) {
                sum += cashFlow.getMoney().getAmount();
            }
        }
        return new Money(sum, Currency.getInstance("HUF"));
    }

    // FILTERING

    /**
     * Updates the models for a specified year, month and a cashflow type
     * 
     * @param yearString the string value of a year (must be parsable into a year)
     * @param month      the month we want to filter for
     * @param type       ALL - if don't want to filter for type.
     *                   INCOME - if you want to filter for only the incomes.
     *                   EXPENSE - if you want to filter for only the expenses.
     * @throws InvalidYearFormatException if the yearString can't be parsed to an int value
     * @throws UpdatingModelFailed if the model could not update
     */
    public void filterFor(String yearString, Month month, CashFlowType type)
            throws InvalidYearFormatException, UpdatingModelFailed {
        int year = parseYear(yearString);
        updateModels(year, month, type);

        selectedYear = year;
        selectedMonth = month;
        selectedCashFlowType = type;
    }

    // MODEL UPDATE
    /**
     * Updates the CashflowTableModel and the SummarizedCashFlowModel of the controller by creating new instances of them.
     * 
     * @param year the CashFlowModel will only contain those records where the years match 
     * @param month the CashFlowModel will only contain those records where the month match
     * @param type the CashFlowModel will only contain those records where the cashflow types match
     * @throws UpdatingModelFailed
     */
    private void updateModels(int year, Month month, CashFlowType type) throws UpdatingModelFailed {
        List<CashFlow> cashFlows;
        try {
            cashFlows = modelSerializer.readAll();
        } catch (SerializerCannotRead e) {
            throw new UpdatingModelFailed("Updating cashflow models failed due to an IO Error");
        }
        double sumOnAccount = 0;
        double sumIncome = 0;
        double sumExpense = 0;
        double sumThisMonth = 0;

        List<CashFlow> listedCashFlow = new ArrayList<>();
        for (CashFlow cashFlow : cashFlows) {
            if (cashFlow.getUser().equals(userLogedIn)) {
                sumOnAccount += cashFlow.getMoney().getAmount();

                if (cashFlow.getDate().getYear() == year && cashFlow.getDate().getMonth().equals(month)) {
                    sumThisMonth += cashFlow.getMoney().getAmount();

                    if (cashFlow.getMoney().getAmount() < 0.0) { // Current cashflow = expense
                        sumExpense += cashFlow.getMoney().getAmount();

                        if (type.equals(CashFlowType.EXPENSE)) {
                            listedCashFlow.add(cashFlow);
                        }
                    } else { // Current cashflow = income
                        sumIncome += cashFlow.getMoney().getAmount();

                        if (type.equals(CashFlowType.INCOME)) {
                            listedCashFlow.add(cashFlow);
                        }
                    }

                    if (type.equals(CashFlowType.ALL)) { // If type was set to ALL -> we can add every cashflow
                        listedCashFlow.add(cashFlow);
                    }
                }
            }
        }

        cashFlowTableModel = new CashFlowTableModel(listedCashFlow);
        summarizedCashFlowModel = new SummarizedCashFlowModel(
                new Money(sumOnAccount, Currency.getInstance("HUF")),
                new Money(sumIncome, Currency.getInstance("HUF")),
                new Money(sumExpense, Currency.getInstance("HUF")),
                new Money(sumThisMonth, Currency.getInstance("HUF")));

    }

    // DATA VALIDATORS AND PARSERS
    /**
     * Tries to parse a string to a possible year value
     * 
     * @param yearString the string you want to parse to a year
     * @return an int value that matches the year
     * @throws InvalidYearFormatException if the yearString is not a number, blank or in the wrong format
     */
    private int parseYear(String yearString) throws InvalidYearFormatException {
        try {
            return Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            throw new InvalidYearFormatException("'" + yearString + "' is not in a valid format");
        }
    }

    /**
     * Checks if a reason is valid or not.
     * 
     * @param reason the reason you want to check
     * @return TRUE - if reason is null or blank. FALSE - if reason is not null and is not blank
     */
    private boolean reasonIsInvalid(String reason) {
        return reason == null || reason.isBlank();
    }

    public enum CashFlowType {
        ALL,
        INCOME,
        EXPENSE
    }

    // FOR-TESTING
    public List<CashFlow> getAllCashFlows() throws SerializerCannotRead {
        return modelSerializer.readAll();
    }
}
