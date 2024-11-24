package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;

class CashFlowControllerTests extends ControllerTests {
    CashFlowController cashFlowController;

    @BeforeEach
    public void setup() {
        cashFlowController = mainFrame.getCashFlowController();
    }

    @Test
    public void testChangeByPositiveAmount() throws Exception {
        cashFlowController.changeMoneyOnAccount(
                LocalDate.of(2000, 01, 01), "1000", Currency.getInstance("HUF"), "Test Reason");

        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();

        assertEquals(1, cashFlows.size());

        CashFlow cashFlow = cashFlows.get(0);
        assertEquals(1, cashFlow.getId());
        assertEquals(LocalDate.of(2000, 01, 01), cashFlow.getDate());
        assertEquals(1000.0, cashFlow.getMoney().getAmount());
        assertEquals(Currency.getInstance("HUF"), cashFlow.getMoney().getCurrency());
        assertEquals("Test Reason", cashFlow.getReason());
    }

    @Test
    public void testChangeByNegativeAmount() throws Exception {
        cashFlowController.changeMoneyOnAccount(LocalDate.of(2000, 01, 01), "-1000", Currency.getInstance("HUF"),
                "Test");

        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();
        assertEquals(1, cashFlows.size());

        assertEquals(-1000, cashFlows.get(0).getMoney().getAmount());
    }

    @Test
    public void testChagneByInvalidAmount1() {
        assertThrows(
                InvalidAmountException.class,
                () -> cashFlowController.changeMoneyOnAccount(LocalDate.of(2000, 01, 01), "Invalid Amount",
                        Currency.getInstance("HUF"), "Test"));
    }

    @Test
    public void testChagneByInvalidAmount2() {
        assertThrows(
                InvalidAmountException.class,
                () -> cashFlowController.changeMoneyOnAccount(LocalDate.of(2000, 01, 01), "",
                        Currency.getInstance("HUF"), "Test"));
    }

    @Test
    public void testChagneByInvalidReason() {
        assertThrows(
                InvalidReasonException.class,
                () -> cashFlowController.changeMoneyOnAccount(LocalDate.of(2000, 01, 01), "1000",
                        Currency.getInstance("HUF"), ""));
    }

    @Test
    public void testSetMoneyOnAccount() throws Exception {
        cashFlowController.changeMoneyOnAccount(LocalDate.of(2000, 01, 01), "500", Currency.getInstance("HUF"), "Test");
        cashFlowController.setMoneyOnAccount("1000", Currency.getInstance("HUF"), "Test Money Set");

        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();
        assertEquals(2, cashFlows.size());

        CashFlow cashFlow = cashFlows.get(1);
        assertEquals(2, cashFlow.getId());
        assertEquals(LocalDate.now(), cashFlow.getDate());
        assertEquals(new Money(500, Currency.getInstance("HUF")), cashFlow.getMoney());
        assertEquals("Test Money Set", cashFlow.getReason());
    }

    @Test
    public void testAddCashFlow() throws Exception {
        cashFlowController.addNewCashFlow(userSignedIn, LocalDate.of(2000, 01, 01), 1500, Currency.getInstance("HUF"),
                "Test");

        assertEquals(1, cashFlowController.getAllCashFlows().size());
        assertEquals(userSignedIn, cashFlowController.getAllCashFlows().get(0).getUser());
    }

    @Test
    public void testDeleteCashFlow() throws Exception {
        CashFlow cashFlow = cashFlowController.addNewCashFlow(userSignedIn, LocalDate.of(2000, 01, 01), 1000,
                Currency.getInstance("HUF"), "Test");
        cashFlowController.deleteCashFlow(cashFlow);

        assertEquals(0, cashFlowController.getAllCashFlows().size());
    }

    @Test
    public void testEditCashFlow() throws Exception {
        CashFlow cashFlow = cashFlowController.addNewCashFlow(userSignedIn, LocalDate.of(2000, 01, 01), 1000,
                Currency.getInstance("HUF"), "Test");
        cashFlowController.editCashFlow(cashFlow, new Money(1500, Currency.getInstance("HUF")), "Test Reason",
                LocalDate.of(2001, 02, 02));

        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();

        assertEquals(1, cashFlows.size());
        CashFlow cashFlowRead = cashFlows.get(0);
        assertEquals(1, cashFlowRead.getId());
        assertEquals(new Money(1500, Currency.getInstance("HUF")), cashFlowRead.getMoney());
        assertEquals("Test Reason", cashFlowRead.getReason());
        assertEquals(LocalDate.of(2001,02,02), cashFlowRead.getDate());
    }

}
