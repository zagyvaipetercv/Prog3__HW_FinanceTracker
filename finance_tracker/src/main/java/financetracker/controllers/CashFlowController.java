package financetracker.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.models.CashFlowTableModel;
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

    private CashFlowType selectedCashFlowType;
    private int selectedYear;
    private Month selectedMonth;

    private int cachedYear;
    private Month cachedMonth;
    private List<CashFlow> cachedCashFlow;

    private Money moneyOnAccount;
    private Money sumIncomes;
    private Money sumExpenses;
    private Money sumThisMonth;

    private CashFlowTableModel cashFlowTableModel;

    public FrameView getChangeMoneyView() {
        return new ChangeMoneyView(this);
    }

    public FrameView getSetMoneyView() {
        return new SetMoneyView(this);
    }

    public PanelView getWalletView() {
        return new WalletView(this, cashFlowTableModel, sumIncomes, sumExpenses, sumThisMonth, moneyOnAccount);
    }

    // VIEW REFRESHER
    public void refreshWalletView() {
        mainFrame.changeView(getWalletView());
    }

    public CashFlowController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEAFULT_SAVE_PATH, mainFrame);
    }

    public CashFlowController(String filePath, MainFrame mainFrame)
            throws ControllerWasNotCreated {
        super(filePath, mainFrame);
        
        try {
            List<CashFlow> allCashFlows = modelSerializer.readAll();
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
    
            sumIncomes = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.INCOME);
            sumExpenses = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.EXPENSE);
            sumThisMonth = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.ALL);
    
            cashFlowTableModel = new CashFlowTableModel(getCashFlows(selectedYear, selectedMonth, CashFlowType.ALL));

        } catch (SerializerCannotRead e) {
            throw new ControllerWasNotCreated("CashFlow controller could not read data", this.getClass());
        }
    }

    // DATA MODIFIERS
    public void changeMoneyOnAccount(LocalDate date, String amountString, Currency currency, String reason)
            throws InvalidReasonException, BalanceCouldNotCahcngeException, InvalidAmountException {

        double amount = parseAmount(amountString);
        addNewCashFlow(date, amount, currency, reason);

        refreshWalletView();
    }

    private void addNewCashFlow(LocalDate date, double amount, Currency currency, String reason)
            throws InvalidReasonException, BalanceCouldNotCahcngeException {
        if (reasonIsInvalid(reason)) {
            throw new InvalidReasonException(reason, "Reason can't be blank");
        }

        Money money = new Money(amount, currency);
        CashFlow cashFlow = new CashFlow(
                modelSerializer.getNextId(),
                date,
                money,
                reason);

        try {
            modelSerializer.appendNewData(cashFlow); // Write data

            // Update view models
            moneyOnAccount = new Money(moneyOnAccount.getAmount() + amount, currency);
            if (!cacheIsInvalid(date.getYear(), date.getMonth())) {
                cachedCashFlow.add(cashFlow);
                cashFlowTableModel = new CashFlowTableModel(cachedCashFlow);
                sumIncomes = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.INCOME);
                sumExpenses = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.EXPENSE);
                sumThisMonth = getSummarizedCashFlow(selectedYear, selectedMonth, CashFlowType.ALL);
            }

        } catch (SerializerCannotRead | SerializerCannotWrite e) {
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
            modelSerializer.appendNewDatas(cashFlowList);
            cachedCashFlow.addAll(cashFlowList);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new BalanceCouldNotCahcngeException(null, "Couldn't add money to balance");
        }
    }

    // DATA QUERIES
    public List<CashFlow> getCashFlows(int year, Month month, CashFlowType type) throws SerializerCannotRead {
        if (cacheIsInvalid(year, month)) {
            reloadCache(year, month);
        }

        List<CashFlow> result = new ArrayList<>();
        for (CashFlow cashFlow : cachedCashFlow) {
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
                    return cachedCashFlow;
            }
        }
        return result;
    }

    public Money getSummarizedCashFlow(int year, Month month, CashFlowType type) throws SerializerCannotRead {
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
            throws InvalidYearFormatException, SerializerCannotRead {
        setFilterOptions(parseYear(yearString), month, type);
    }

    void setFilterOptions(int year, Month month, CashFlowType type) throws SerializerCannotRead {
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

    private void reloadCache(int year, Month month) throws SerializerCannotRead {
        List<CashFlow> saved = modelSerializer.readAll();
        cachedCashFlow = new ArrayList<>();

        // Update cached meta data
        cachedYear = year;
        cachedMonth = month;

        // Update cached cash flows
        for (CashFlow cashFlow : saved) {
            int cashFlowYear = cashFlow.getDate().getYear();
            Month cashFlowMonth = cashFlow.getDate().getMonth();
            if (cashFlowYear == year && cashFlowMonth.equals(month)) {
                cachedCashFlow.add(cashFlow);
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
