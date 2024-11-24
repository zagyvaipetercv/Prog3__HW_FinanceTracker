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
    public CashFlowController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEAFULT_SAVE_PATH, mainFrame);
    }

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
    public PanelView getWalletView() throws UpdatingModelFailed {
        updateModels(selectedYear, selectedMonth, selectedCashFlowType);
        return new WalletView(this,
                cashFlowTableModel, summarizedCashFlowModel,
                selectedYear, selectedMonth, selectedCashFlowType);
    }

    public FrameView getChangeMoneyView() {
        return new ChangeMoneyView(this);
    }

    public FrameView getSetMoneyView() {
        return new SetMoneyView(this);
    }

    // VIEW REFRESHER
    public void refreshWalletView() throws UpdatingModelFailed {
        mainFrame.changeView(getWalletView());
    }

    // ACTIONS
    public void changeMoneyOnAccount(LocalDate date, String amountString, Currency currency, String reason)
            throws InvalidReasonException, CreatingRecordFailed, InvalidAmountException, UpdatingModelFailed {

        double amount = Money.parseAmount(amountString);
        addNewCashFlow(userLogedIn, date, amount, currency, reason);
        updateModels(selectedYear, selectedMonth, selectedCashFlowType);
    }

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

    public void setMoneyOnAccount(String newAmountString, Currency currency, String reason)
            throws InvalidAmountException, CreatingRecordFailed, InvalidReasonException, UpdatingModelFailed {
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

    public void deleteCashFlow(CashFlow cashFlow) throws DeletingRecordFailed {
        try {
            modelSerializer.removeData(cashFlow.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingRecordFailed("Deleting Cashflow failed", cashFlow);
        }
    }

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

    // FILTERING
    public void filterFor(String yearString, Month month, CashFlowType type)
            throws InvalidYearFormatException, UpdatingModelFailed {
        int year = parseYear(yearString);
        updateModels(year, month, type);

        selectedYear = year;
        selectedMonth = month;
        selectedCashFlowType = type;
    }

    // GETTERS
    public int getSelectedYear() {
        return selectedYear;
    }

    public Month getSelectedMonth() {
        return selectedMonth;
    }

    public CashFlowType getSelectedCashFlowType() {
        return selectedCashFlowType;
    }

    private Money getMoneyOnAccount() throws SerializerCannotRead {
        double sum = 0;
        for (CashFlow cashFlow : modelSerializer.readAll()) {
            if (cashFlow.getUser().equals(userLogedIn)) {
                sum += cashFlow.getMoney().getAmount();
            }
        }
        return new Money(sum, Currency.getInstance("HUF"));
    }

    // MODEL UPDATE
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
    private int parseYear(String yearString) throws InvalidYearFormatException {
        try {
            return Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            throw new InvalidYearFormatException("'" + yearString + "' is not in a valid format");
        }
    }

    private boolean reasonIsInvalid(String reason) {
        return reason == null || reason.isBlank();
    }

    public enum CashFlowType {
        ALL,
        INCOME,
        EXPENSE
    }
}
