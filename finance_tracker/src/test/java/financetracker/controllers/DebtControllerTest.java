package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.controllers.DebtController.DebtDirection;
import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.debtcontroller.PaymentIsGreaterThanRemaining;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.usercontroller.UserNotFound;

class DebtControllerTest extends ControllerTests {

    DebtController debtController;
    CashFlowController cashFlowController;
    UserController userController;

    @BeforeEach
    void setup() {
        debtController = mainFrame.getDebtController();
        cashFlowController = mainFrame.getCashFlowController();
        userController = mainFrame.getUserController();
    }

    @Test
    void testAddDebts() throws Exception {
        // ACT
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));
        debtController.addDebt(userName3, DebtDirection.THEY_OWE, LocalDate.of(2000, 01, 01), "2500", true,
                LocalDate.of(2000, 01, 03));

        // ASSERTS
        List<Debt> debts = debtController.getAllDebts();
        assertEquals(2, debts.size());

        Debt debt1 = debts.get(0);
        assertEquals(1, debt1.getId());
        assertEquals(userSignedIn, debt1.getDebtor());
        assertEquals(userController.findUser(userName2), debt1.getCreditor());
        assertEquals(LocalDate.of(2000, 01, 01), debt1.getDate());
        assertEquals(new Money(2000, Currency.getInstance("HUF")), debt1.getDebtAmount());
        assertEquals(new Money(0, Currency.getInstance("HUF")), debt1.getPayedAmount());
        assertEquals(false, debt1.hasDeadline());
        assertEquals(null, debt1.getDeadline());

        Debt debt2 = debts.get(1);
        assertEquals(2, debt2.getId());
        assertEquals(userController.findUser(userName3), debt2.getDebtor());
        assertEquals(userSignedIn, debt2.getCreditor());
        assertEquals(LocalDate.of(2000, 01, 01), debt2.getDate());
        assertEquals(new Money(2500, Currency.getInstance("HUF")), debt2.getDebtAmount());
        assertEquals(new Money(0, Currency.getInstance("HUF")), debt2.getPayedAmount());
        assertEquals(true, debt2.hasDeadline());
        assertEquals(LocalDate.of(2000, 01, 03), debt2.getDeadline());
    }

    @Test
    void testAddDebtToNonExistingUser() {
        assertThrows(
                UserNotFound.class,
                () -> debtController.addDebt("Mr. DoesNotExist", DebtDirection.I_OWE, LocalDate.of(2000, 01, 01),
                        "2000", false, LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testAddDebtWithUnsetDirection() {
        assertThrows(
                CreatingRecordFailed.class,
                () -> debtController.addDebt(userName2, DebtDirection.UNSET, LocalDate.of(2000, 01, 01), "2000", false,
                        LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testAddDebtToSelf() {
        assertThrows(
                CreatingRecordFailed.class,
                () -> debtController.addDebt(userLogedInName, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000",
                        false, LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testAddDebtWithBlankAmount() {
        assertThrows(
                InvalidAmountException.class,
                () -> debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), " ", false,
                        LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testAddDebtWithNegativeAmount() {
        assertThrows(
                InvalidAmountException.class,
                () -> debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "-2000", false,
                        LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testAddDebtWithWrongFormatedAmount() {
        assertThrows(
                InvalidAmountException.class,
                () -> debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "Ketezer",
                        false, LocalDate.of(2000, 01, 01)));
    }

    @Test
    void testEditDebt() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));

        debtController.addDebt(userName3, DebtDirection.THEY_OWE, LocalDate.of(2000, 01, 01), "2500", true,
                LocalDate.of(2000, 01, 03));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.editDebt(debt, LocalDate.of(2000, 01, 03), "3000", true, LocalDate.of(2000, 01, 04));

        // ASSERTS
        List<Debt> debts = debtController.getAllDebts();
        assertEquals(2, debts.size());

        Debt debt1 = debts.get(0);
        assertEquals(1, debt1.getId());
        assertEquals(userSignedIn, debt1.getDebtor());
        assertEquals(userController.findUser(userName2), debt1.getCreditor());
        assertEquals(LocalDate.of(2000, 01, 03), debt1.getDate());
        assertEquals(new Money(3000, Currency.getInstance("HUF")), debt1.getDebtAmount());
        assertEquals(new Money(0, Currency.getInstance("HUF")), debt1.getPayedAmount());
        assertEquals(true, debt1.hasDeadline());
        assertEquals(LocalDate.of(2000, 01, 04), debt1.getDeadline());

        Debt debt2 = debts.get(1);
        assertEquals(2, debt2.getId());
        assertEquals(userController.findUser(userName3), debt2.getDebtor());
        assertEquals(userSignedIn, debt2.getCreditor());
        assertEquals(LocalDate.of(2000, 01, 01), debt2.getDate());
        assertEquals(new Money(2500, Currency.getInstance("HUF")), debt2.getDebtAmount());
        assertEquals(new Money(0, Currency.getInstance("HUF")), debt2.getPayedAmount());
        assertEquals(true, debt2.hasDeadline());
        assertEquals(LocalDate.of(2000, 01, 03), debt2.getDeadline());
    }

    @Test
    void testDeleteDebt() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));
        debtController.addDebt(userName3, DebtDirection.THEY_OWE, LocalDate.of(2000, 01, 01), "2500", true,
                LocalDate.of(2000, 01, 03));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.deleteDebt(debt);

        // ASSERT
        List<Debt> debts = debtController.getAllDebts();
        assertEquals(1, debts.size());

        Debt debt2 = debts.get(0);
        assertEquals(2, debt2.getId());
        assertEquals(userController.findUser(userName3), debt2.getDebtor());
        assertEquals(userSignedIn, debt2.getCreditor());
        assertEquals(LocalDate.of(2000, 01, 01), debt2.getDate());
        assertEquals(new Money(2500, Currency.getInstance("HUF")), debt2.getDebtAmount());
        assertEquals(new Money(0, Currency.getInstance("HUF")), debt2.getPayedAmount());
        assertEquals(true, debt2.hasDeadline());
        assertEquals(LocalDate.of(2000, 01, 03), debt2.getDeadline());
    }

    @Test
    void testRepayDebt() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.payDebt(debt, "1000", LocalDate.of(2000, 01, 01));
        debtController.payDebt(debt, "500", LocalDate.of(2000, 01, 01));

        // ASSERT
        debt = debtController.getAllDebts().get(0);
        assertEquals(1500, debt.getPayedAmount().getAmount());
        assertEquals(false, debt.isFulfilled());

    }

    @Test
    void testRepayDebtUntilFulfilled() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.payDebt(debt, "1000", LocalDate.of(2000, 01, 01));
        debtController.payDebt(debt, "1000", LocalDate.of(2000, 01, 01));

        // ASSERT
        debt = debtController.getAllDebts().get(0);
        assertEquals(2000, debt.getPayedAmount().getAmount());
        assertEquals(true, debt.isFulfilled());
    }

    @Test
    void testRepayDebtOverFulfilled() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.payDebt(debt, "1000", LocalDate.of(2000, 01, 01));

        assertThrows(PaymentIsGreaterThanRemaining.class,
                () -> {
                    Debt debtTmp = debtController.getAllDebts().get(0);
                    debtController.payDebt(debtTmp, "1500", LocalDate.of(2000, 01, 01));
                });

        // ASSERT
        debt = debtController.getAllDebts().get(0);
        assertEquals(2000, debt.getPayedAmount().getAmount());
        assertEquals(true, debt.isFulfilled());
    }

    @Test
    void testRepayAll() throws Exception {
        // PRE
        debtController.addDebt(userName2, DebtDirection.I_OWE, LocalDate.of(2000, 01, 01), "2000", false,
                LocalDate.of(2000, 01, 02));

        // ACT
        Debt debt = debtController.getAllDebts().get(0);
        debtController.payDebt(debt, "1000", LocalDate.of(2000, 01, 01));
        debtController.payAll(debt, LocalDate.of(2000, 01, 01));

        // ASSERT
        debt = debtController.getAllDebts().get(0);
        assertEquals(2000, debt.getPayedAmount().getAmount());
        assertEquals(true, debt.isFulfilled());
    }

}
