package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.controllers.CashFlowController.CashFlowType;
import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.InvalidYearFormatException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;

class CashFlowControllerTests extends ControllerTests {

    private CashFlowController controller;

    @BeforeEach
    public void setupController() throws ControllerWasNotCreated  {
        controller = new CashFlowController(TEST_SAVE_FILE_PATH, null);
    }

    @Test
    void checkDefaultInit() {
        LocalDate defaultDate = LocalDate.now();
        double delta = 0.01;

        assertEquals(defaultDate.getYear(), controller.getSelectedYear());
        assertEquals(defaultDate.getMonth(), controller.getSelectedMonth());
        assertEquals(CashFlowController.CashFlowType.ALL, controller.getSelectedCashFlowType());
        assertEquals(0.0, controller.getMoneyOnAccount().getAmount(), delta);
        assertEquals(Currency.getInstance("HUF"), controller.getMoneyOnAccount().getCurrency());
    }

    @Test
    void checkSetFilters() throws InvalidYearFormatException, SerializerCannotRead {
        controller.setFilterOptions("2000", Month.JANUARY, CashFlowType.INCOME);
        assertEquals(2000, controller.getSelectedYear());
        assertEquals(Month.JANUARY, controller.getSelectedMonth());
        assertEquals(CashFlowType.INCOME, controller.getSelectedCashFlowType());
    }

    @Test
    void checkSetFiltersWrongYearFormat() {
        assertThrows(InvalidYearFormatException.class, () -> {
            controller.setFilterOptions("KettoEzer", Month.JANUARY, CashFlowType.ALL);
        });
    }

    @Test
    void checkGetCashFlowOnEmptySaveFile() throws SerializerCannotRead  {
        // Pre
        controller.setFilterOptions(2001, Month.FEBRUARY, CashFlowType.EXPENSE);
        double delta = 0.01;

        // Act
        List<CashFlow> cashFlowList = controller.getCashFlows(2000, Month.JANUARY, CashFlowType.ALL);

        // Asserts
        assertEquals(0, cashFlowList.size());
        assertEquals(2001, controller.getSelectedYear());
        assertEquals(Month.FEBRUARY, controller.getSelectedMonth());
        assertEquals(CashFlowType.EXPENSE, controller.getSelectedCashFlowType());
        assertEquals(0.0, controller.getMoneyOnAccount().getAmount(), delta);
    }

    @Test
    void checkChangeMoneyOnAccount() throws InvalidReasonException, BalanceCouldNotCahcngeException,
            InvalidAmountException, SerializerCannotRead {
        // Pre
        LocalDate dateOfChange = LocalDate.of(2000, 01, 01);
        String amountString = "2000.5";
        Currency currency = Currency.getInstance("HUF");
        String reason = "Test";

        // Act
        controller.setFilterOptions(2020, Month.DECEMBER, CashFlowType.INCOME);
        controller.changeMoneyOnAccount(dateOfChange, amountString, currency, reason);
        controller.changeMoneyOnAccount(dateOfChange, amountString, currency, reason);
        List<CashFlow> cashFlowList = controller.getCashFlows(2000, Month.JANUARY, CashFlowType.ALL);

        // Asserts
        // selected fields not changed
        assertEquals(2020, controller.getSelectedYear());
        assertEquals(Month.DECEMBER, controller.getSelectedMonth());
        assertEquals(CashFlowType.INCOME, controller.getSelectedCashFlowType());

        // queried data
        assertEquals(2, cashFlowList.size());

        CashFlow cashFlow = cashFlowList.get(0);
        assertEquals(1, cashFlow.getId());
        assertEquals(2000.5, cashFlow.getMoney().getAmount());
        assertEquals(Currency.getInstance("HUF"), cashFlow.getMoney().getCurrency());
        assertEquals(LocalDate.of(2000, 1, 1), cashFlow.getDate());
        assertEquals("Test", cashFlow.getReason());

        cashFlow = cashFlowList.get(1);
        assertEquals(2, cashFlow.getId());
        assertEquals(2000.5, cashFlow.getMoney().getAmount());
        assertEquals(Currency.getInstance("HUF"), cashFlow.getMoney().getCurrency());
        assertEquals(LocalDate.of(2000, 1, 1), cashFlow.getDate());
        assertEquals("Test", cashFlow.getReason());
    }

    @Test
    void checkChangeByInvalidAmount() {
        assertThrows(
                InvalidAmountException.class,
                () -> controller.changeMoneyOnAccount(
                        LocalDate.of(2000, 01, 01),
                        "2000,5",
                        Currency.getInstance("HUF"),
                        "Test"));

        assertThrows(
                InvalidAmountException.class,
                () -> controller.changeMoneyOnAccount(
                        LocalDate.of(2000, 01, 01),
                        "Kettoezer",
                        Currency.getInstance("HUF"),
                        "Test"));
    }

    @Test
    void checkChangeInvalidReason() {
        // Act
        assertThrows(
                InvalidReasonException.class,
                () -> controller.changeMoneyOnAccount(
                        LocalDate.of(2000, 01, 01),
                        "2000",
                        Currency.getInstance("HUF"),
                        "")); // Empty String

        assertThrows(
            InvalidReasonException.class,
                () -> controller.changeMoneyOnAccount(
                        LocalDate.of(2000, 01, 01),
                        "2000",
                        Currency.getInstance("HUF"),
                        "  ")); // Only Whitespace
    }

    @Test
    void checkSummarizedCashFlows()
            throws InvalidReasonException, BalanceCouldNotCahcngeException, InvalidAmountException, SerializerCannotRead {

        // Act
        controller.changeMoneyOnAccount(
                LocalDate.of(2020, 1, 1),
                "2000",
                Currency.getInstance("HUF"),
                "Income20");

        controller.changeMoneyOnAccount(
                LocalDate.of(2020, 1, 1),
                "-1000",
                Currency.getInstance("HUF"),
                "Income20");

        controller.changeMoneyOnAccount(
                LocalDate.of(2021, 1, 1),
                "-1000",
                Currency.getInstance("HUF"),
                "Expense21");

        // Assert
        Money all20 = controller.getSummarizedCashFlow(2020, Month.JANUARY, CashFlowType.ALL);
        Money income20 = controller.getSummarizedCashFlow(2020, Month.JANUARY, CashFlowType.INCOME);
        Money expense20 = controller.getSummarizedCashFlow(2020, Month.JANUARY, CashFlowType.EXPENSE);

        Money all21 = controller.getSummarizedCashFlow(2021, Month.JANUARY, CashFlowType.ALL);
        Money income21 = controller.getSummarizedCashFlow(2021, Month.JANUARY, CashFlowType.INCOME);
        Money expense21 = controller.getSummarizedCashFlow(2021, Month.JANUARY, CashFlowType.EXPENSE);

        assertEquals(new Money(1000, Currency.getInstance("HUF")), all20);
        assertEquals(new Money(2000, Currency.getInstance("HUF")), income20);
        assertEquals(new Money(-1000, Currency.getInstance("HUF")), expense20);

        assertEquals(new Money(-1000, Currency.getInstance("HUF")), all21);
        assertEquals(new Money(0, Currency.getInstance("HUF")), income21);
        assertEquals(new Money(-1000, Currency.getInstance("HUF")), expense21);

        assertEquals(new Money(0.0, Currency.getInstance("HUF")), controller.getMoneyOnAccount());
    }

    @Test
    void checkSetMoneyOnAccount()
            throws InvalidAmountException, BalanceCouldNotCahcngeException, InvalidReasonException, SerializerCannotRead {

        LocalDate dateOfChange = LocalDate.now();
        // Act
        controller.setMoneyOnAccount(
                "1000",
                Currency.getInstance("HUF"),
                "Test");

        // Assert
        List<CashFlow> cashFlowList = controller.getCashFlows(dateOfChange.getYear(), dateOfChange.getMonth(),
                CashFlowType.ALL);
        assertEquals(1, cashFlowList.size());

        CashFlow cashFlow = cashFlowList.get(0);
        assertEquals(1000, cashFlow.getMoney().getAmount());
    }

    @Test
    void checkSetMoneyOnAccountInvalidAmount() {
        assertThrows(
                InvalidAmountException.class,
                () -> controller.setMoneyOnAccount(
                        "200,0",
                        Currency.getInstance("HUF"),
                        "Test"));
    }
}
