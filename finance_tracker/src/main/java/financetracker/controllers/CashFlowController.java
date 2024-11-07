package financetracker.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.MoneyAmountIsInvalidException;
import financetracker.exceptions.cashflowcontroller.ReasonIsInvalidException;
import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.exceptions.controller.ControllerCannotReadException;
import financetracker.exceptions.controller.ControllerCannotWriteException;
import financetracker.models.CashFlow;
import financetracker.models.Money;
import financetracker.views.bases.FrameView;
import financetracker.views.bases.PanelView;
import financetracker.views.cashflow.ChangeMoneyView;
import financetracker.views.cashflow.SetMoneyView;
import financetracker.views.cashflow.WalletView;
import financetracker.windowing.MainFrame;

// TODO: Create test cases for MoneyController
public class CashFlowController extends Controller<CashFlow> {

    private static final String DEFAULT_FILEPATH = "saves\\cash_flow.dat";

    private int selectedYear;
    private Month selectedMonth;
    private List<CashFlow> cachedTableData;

    private Money moneyOnAccount;

    public FrameView getChangeMoneyView() {
        return new ChangeMoneyView(this);
    }

    public FrameView getSetMoneyView() {
        return new SetMoneyView(this);
    }

    public PanelView getWalletView() {
        return new WalletView(this);
    }

    public CashFlowController(MainFrame mainFrame) throws CannotCreateControllerException, ControllerCannotReadException {
        this(DEFAULT_FILEPATH, mainFrame);
    }

    public CashFlowController(String filePath, MainFrame mainFrame) throws CannotCreateControllerException, ControllerCannotReadException {
        super(filePath, mainFrame);
        List<CashFlow> allCashFlows = readAll();
        double sum = 0.0;
        for (CashFlow cashFlow : allCashFlows) {
            sum += cashFlow.getMoney().getAmount();
        }

        moneyOnAccount = new Money(sum, Currency.getInstance("HUF"));
    }

    private void refresh() {
        mainFrame.changeView(getWalletView());
    }

    public void changeMoneyOnAccount(LocalDate date, String amountString, Currency currency, String reason)
            throws ReasonIsInvalidException, BalanceCouldNotCahcngeException, MoneyAmountIsInvalidException {

        if (reasonIsInvalid(reason)) {
            throw new ReasonIsInvalidException(reason, "Reason can't be blank");
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
            if (!cacheIsInvalid(cashFlow.getDate().getYear(), cashFlow.getDate().getMonth())) {
                cachedTableData.add(cashFlow);
            }
            refresh();
        } catch (ControllerCannotWriteException | ControllerCannotReadException e) {
            throw new BalanceCouldNotCahcngeException(money, "Couldn't add money to balance");
        }
    }

    public void setMoneyOnAccount(String newAmountString, Currency currency, String reason) throws MoneyAmountIsInvalidException, BalanceCouldNotCahcngeException, ReasonIsInvalidException {
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
            refresh();
        } catch (ControllerCannotReadException | ControllerCannotWriteException e) {
            throw new BalanceCouldNotCahcngeException(null, "Couldn't add money to balance");
        }
    }

    private boolean cacheIsInvalid(int year, Month month) {
        return (selectedYear != year || !selectedMonth.equals(month));
    }

    private void reloadCache(int year, Month month) throws ControllerCannotReadException {
        selectedYear = year;
        selectedMonth = month;

        List<CashFlow> saved = readAll();
        cachedTableData = new ArrayList<>();

        for (CashFlow cashFlow : saved) {
            int cashFlowYear = cashFlow.getDate().getYear();
            Month cashFlowMonth = cashFlow.getDate().getMonth();
            if (cashFlowYear == year && cashFlowMonth.equals(month)) {
                cachedTableData.add(cashFlow);
            }
        }
    }

    // TODO: Replace with a specialized exception (like CannotReadMoneyException or
    // somehting)
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

    private double parseAmount(String amountString) throws MoneyAmountIsInvalidException {
        try {
            if (amountString.isBlank()) {
                throw new MoneyAmountIsInvalidException(amountString);
            }
            return Double.parseDouble(amountString);
        } catch (NullPointerException | NumberFormatException e) {
            throw new MoneyAmountIsInvalidException(amountString);
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
