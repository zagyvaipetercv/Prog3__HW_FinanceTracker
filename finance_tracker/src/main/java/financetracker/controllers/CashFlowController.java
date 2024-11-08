package financetracker.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.models.CashFlow;
import financetracker.models.Money;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.cashflow.ChangeMoneyView;
import financetracker.views.cashflow.SetMoneyView;
import financetracker.views.cashflow.WalletView;
import financetracker.windowing.MainFrame;

public class CashFlowController extends Controller<CashFlow> {

    private static final String DEFAULT_FILEPATH = "saves\\cash_flow.dat";

    private CashFlowType selectedCashFlowType;
    private int selectedYear;
    private Month selectedMonth;

    private Money moneyOnAccount;

    private int cachedYear;
    private Month cachedMonth;
    private List<CashFlow> cachedTableData;

    // VIEW GETTERS
    public FrameView getChangeMoneyView() {
        return new ChangeMoneyView(this);
    }

    public FrameView getSetMoneyView() {
        return new SetMoneyView(this);
    }

    public PanelView getWalletView() {
        return new WalletView(this);
    }

    // VIEW REFRESHER
    public void refreshWalletView() {
        mainFrame.changeView(getWalletView());
    }

    // CONSTRUCTORS
    public CashFlowController(MainFrame mainFrame)
            throws CannotCreateControllerException, ControllerCannotReadException {
        this(DEFAULT_FILEPATH, mainFrame);
    }

    public CashFlowController(String filePath, MainFrame mainFrame)
            throws CannotCreateControllerException, ControllerCannotReadException {
        super(filePath, mainFrame);
        List<CashFlow> allCashFlows = readAll();
        double sum = 0.0;
        for (CashFlow cashFlow : allCashFlows) {
            sum += cashFlow.getMoney().getAmount();
        }

        moneyOnAccount = new Money(sum, Currency.getInstance("HUF"));

        LocalDate defaultDate = LocalDate.now();
        selectedYear = defaultDate.getYear();
        selectedMonth = defaultDate.getMonth();
        selectedCashFlowType = CashFlowType.ALL;

        reloadCache(selectedYear, selectedMonth);
    }

    // DATA MODIFIERS
    public void changeMoneyOnAccount(LocalDate date, String amountString, Currency currency, String reason)
            throws InvalidReasonException, BalanceCouldNotCahcngeException, InvalidAmountException {

        if (reasonIsInvalid(reason)) {
            throw new InvalidReasonException(reason, "Reason can't be blank");
        }

        double amount = parseAmount(amountString);
        Money money = new Money(amount, currency);
        CashFlow cashFlow = new CashFlow(
                getNextId(),
                date,
                money,
                reason);
        try {
            appendNewData(cashFlow);
            moneyOnAccount = new Money(moneyOnAccount.getAmount() + amount, currency);
            if (!cacheIsInvalid(selectedYear, selectedMonth)) {
                cachedTableData.add(cashFlow);
            }
        } catch (ControllerCannotWriteException | ControllerCannotReadException e) {
            throw new BalanceCouldNotCahcngeException(money, "Couldn't add money to balance");
        }
    }

    public void setMoneyOnAccount(String newAmountString, Currency currency, String reason)
            throws InvalidAmountException, BalanceCouldNotCahcngeException, InvalidReasonException {
        double newAmount = parseAmount(newAmountString);
        LocalDate today = LocalDate.now();
        double currentAmount = moneyOnAccount.getAmount();
        double difference = newAmount - currentAmount;
        changeMoneyOnAccount(today, String.valueOf(difference), currency, reason);
    }

    public void addListOfCashFlow(List<CashFlow> cashFlowList) throws BalanceCouldNotCahcngeException {
        try {
            appendNewDatas(cashFlowList);
            cachedTableData.addAll(cashFlowList);
        } catch (ControllerCannotReadException | ControllerCannotWriteException e) {
            throw new BalanceCouldNotCahcngeException(null, "Couldn't add money to balance");
        }
    }

    // DATA QUERIES
    public List<CashFlow> getCashFlows(int year, Month month, CashFlowType type) throws ControllerCannotReadException {
        if (cacheIsInvalid(year, month)) {
            reloadCache(year, month);
        }

        List<CashFlow> result = new ArrayList<>();
        for (CashFlow cashFlow : cachedTableData) {
            switch (type) {
                case INCOME:
                    if (cashFlow.getMoney().getAmount() > 0) {
                        result.add(cashFlow);
                    }
                    break;

                case EXPENSE:
                    if (cashFlow.getMoney().getAmount() < 0) {
                        result.add(cashFlow);
                    }
                    break;

                default:
                    return cachedTableData;
            }
        }
        return result;
    }

    public Money getSummarizedCashFlow(int year, Month month, CashFlowType type) throws ControllerCannotReadException {
        List<CashFlow> cashFlowList = getCashFlows(year, month, type);
        double sum = 0.0;
        for (CashFlow cashFlow : cashFlowList) {
            sum += cashFlow.getMoney().getAmount();
        }

        return new Money(sum, Currency.getInstance("HUF"));
    }

    public Money getMoneyOnAccount() {
        return moneyOnAccount;
    }

    // SETTERS
    public void setFilterOptions(String yearString, Month month, CashFlowType type)
            throws ControllerCannotReadException, InvalidYearFormatException {
        setFilterOptions(parseYear(yearString), month, type);
    }

    void setFilterOptions(int year, Month month, CashFlowType type) throws ControllerCannotReadException {
        selectedYear = year;
        selectedMonth = month;
        selectedCashFlowType = type;
        if (cacheIsInvalid(selectedYear, selectedMonth)) {
            reloadCache(selectedYear, selectedMonth);
        }
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

    // CACHED DATA
    private boolean cacheIsInvalid(int year, Month month) {
        return (year != cachedYear || !month.equals(cachedMonth));
    }

    private void reloadCache(int year, Month month) throws ControllerCannotReadException {
        List<CashFlow> saved = readAll();
        cachedTableData = new ArrayList<>();

        // Update cached meta data
        cachedYear = year;
        cachedMonth = month;

        // Update cached cash flows
        for (CashFlow cashFlow : saved) {
            int cashFlowYear = cashFlow.getDate().getYear();
            Month cashFlowMonth = cashFlow.getDate().getMonth();
            if (cashFlowYear == year && cashFlowMonth.equals(month)) {
                cachedTableData.add(cashFlow);
            }
        }
    }

    // DATA VALIDATORS AND PARSERS
    private int parseYear(String yearString) throws InvalidYearFormatException {
        try {
            return Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            throw new InvalidYearFormatException("'" + yearString + "' is not in a valid format");
        }
    }

    private double parseAmount(String amountString) throws InvalidAmountException {
        try {
            if (amountString.isBlank()) {
                throw new InvalidAmountException(amountString, "Amount can ot be blank");
            }
            return Double.parseDouble(amountString);
        } catch (NullPointerException | NumberFormatException e) {
            throw new InvalidAmountException(amountString, "'" + amountString
                    + "' is not a number or in the wrong format (allowed characters are numbers and '.')");
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
