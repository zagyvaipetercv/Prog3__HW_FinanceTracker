package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JList;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.datatypes.Payment;
import financetracker.datatypes.User;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.debtcontroller.DeptPaymentFailedException;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.exceptions.debtcontroller.PaymentIsGreaterThanRemaining;
import financetracker.exceptions.debtcontroller.UnknownDebtDirection;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.generic.DeletingRecordFailed;
import financetracker.exceptions.generic.EditingRecordFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.exceptions.models.NoItemWasSelected;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.models.DebtListModel;
import financetracker.utilities.CustomMath;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.debt.AddNewDebtView;
import financetracker.views.debt.AddPaymentView;
import financetracker.views.debt.DebtView;
import financetracker.views.debt.EditSelectedDebtView;
import financetracker.windowing.MainFrame;

public class DebtController extends Controller<Debt> {
    private static final String DEFAULT_SAVE_PATH = "saves\\debt.dat";

    // FITLER
    private DebtDirection filterDirection;
    private DebtFulfilled filterIsFulfilled;
    private User fitleredUser;

    // MODEL
    private DebtListModel debtListModel;

    // USED CONTROLLERS
    private UserController userController;
    private CashFlowController cashFlowController;

    // USER
    private User userLogedIn;

    // CONSTRUCTORS
    public DebtController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);

        userLogedIn = mainFrame.getUserLogedIn();

        userController = mainFrame.getUserController();
        cashFlowController = mainFrame.getCashFlowController();

        filterDirection = DebtDirection.UNSET;
        filterIsFulfilled = DebtFulfilled.ALL;
        fitleredUser = null;
    }

    public DebtController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    // VIEW GETTERS
    public PanelView getDebtView() throws UpdatingModelFailed {
        updateDebtListModel(filterDirection, filterIsFulfilled, fitleredUser);
        return new DebtView(this, debtListModel);
    }

    public FrameView getEditSelectedDebtView(JList<? extends Debt> debtsList)
            throws NoItemWasSelected, FulfilledDebtCantChange {

        Debt selected = getSelectedItem(debtsList);

        if (selected.isFulfilled()) {
            throw new FulfilledDebtCantChange("Can't edit a debt that is already fulfilled", selected);
        }

        return new EditSelectedDebtView(this, selected);
    }

    public FrameView getAddNewDebtView() {
        return new AddNewDebtView(this);
    }

    public FrameView getAddPaymentView(JList<? extends Debt> debtList)
            throws NoItemWasSelected, FulfilledDebtCantChange {
        Debt selected = getSelectedItem(debtList);

        if (selected.isFulfilled()) {
            throw new FulfilledDebtCantChange("Can't repay an already fulfilled debt", selected);
        }

        return new AddPaymentView(this, selected);
    }

    public void refreshDebtView() throws UpdatingModelFailed {
        updateDebtListModel(filterDirection, filterIsFulfilled, fitleredUser);
        mainFrame.changeView(getDebtView());
    }

    // ACTIONS
    public void addDebt(String name, DebtDirection direction, LocalDate date, String amountString, boolean hasDeadline,
            LocalDate deadline) throws UserNotFound, InvalidAmountException, CreatingRecordFailed {
        long id = modelSerializer.getNextId();
        User counterParty = findCounterParty(name);
        double amount = Money.parseAmount(amountString);

        validateAmount(amount, null);

        Money money = new Money(amount, Currency.getInstance("HUF"));
        if (counterParty.equals(userLogedIn)) {
            throw new CreatingRecordFailed("Other party can't be the user loged in", null);
        }

        User debtor;
        User creditor;
        try {
            debtor = getDebtor(counterParty, direction);
            creditor = getCreditor(counterParty, direction);
        } catch (UnknownDebtDirection e) {
            throw new CreatingRecordFailed("Creating debt failed due to unkonw direction", null);
        }

        Debt debt = new Debt(id, debtor, creditor, date, money, new ArrayList<>(),
                (hasDeadline ? deadline : null));
        try {
            modelSerializer.appendNewData(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingRecordFailed("Creating debt failed due to an IO Error", debt);
        }
    }

    public void editDebt(Debt debt, LocalDate date, String amountString, boolean hasDeadline, LocalDate deadline)
            throws InvalidAmountException, EditingRecordFailed {

        double amount = Money.parseAmount(amountString);
        validateAmount(amount, debt);

        Money money = new Money(amount, Currency.getInstance("HUF"));

        debt.setDate(date);
        debt.setDebtAmount(money);
        debt.setDeadline((hasDeadline ? deadline : null));

        try {
            modelSerializer.changeData(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingRecordFailed("Editing record failed due to IO Error", debt);
        }
    }

    public void repayDebt(Debt debt, String amountString, LocalDate date)
            throws InvalidAmountException, DeptPaymentFailedException, PaymentIsGreaterThanRemaining {
        double amount = Money.parseAmount(amountString);

        double remainingDebtAmount = debt.getDebtAmount().getAmount() - Debt.repayed(debt).getAmount();

        if (paymentAmountIsInvalid(amount)) {
            throw new InvalidAmountException(amountString, "Cant't repay 0 or less");
        }

        if (CustomMath.almostEquals(amount, remainingDebtAmount)) {
            amount = remainingDebtAmount;
        }

        if (remainingDebtAmount < amount) {
            repay(debt, remainingDebtAmount, date);
            throw new PaymentIsGreaterThanRemaining(
                    "Payed amount is greater than remaining. Only remaining will be payed",
                    new Money(remainingDebtAmount, Currency.getInstance("HUF")),
                    new Money(amount, Currency.getInstance("HUF")));
        }

        repay(debt, amount, date);
    }

    public void repayAll(Debt debt, LocalDate date) throws DeptPaymentFailedException {
        double remaining = debt.getDebtAmount().getAmount() - Debt.repayed(debt).getAmount();

        repay(debt, remaining, date);
    }

    public void deleteDebt(Debt debt) throws DeletingRecordFailed {
        try {
            for (Payment payment : debt.getPayments()) {
                cashFlowController.deleteCashFlow(payment.getDebtorsCashFlow());
                cashFlowController.deleteCashFlow(payment.getCreditorsCashFlow());
            }
            modelSerializer.removeData(debt.getId());
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingRecordFailed("Debt was not removed due to an IO Error", debt);
        } catch (DeletingRecordFailed e) {
            throw new DeletingRecordFailed("Debt could not delete cashflows", debt);
        }
    }

    public void filterFor(DebtDirection direction, DebtFulfilled fulfilled, String userName)
            throws UserNotFound, UpdatingModelFailed {

        User user = null;
        if (!userName.isEmpty()) {
            user = userController.findUser(userName);
        }
        updateDebtListModel(direction, fulfilled, user);
        refreshDebtView();
    }

    // HELPER METHODS
    private void updateDebtListModel(DebtDirection direction, DebtFulfilled fulfilled, User user)
            throws UpdatingModelFailed {
        List<Debt> listedDebts;
        try {
            listedDebts = new ArrayList<>(modelSerializer.readAll());
        } catch (SerializerCannotRead e) {
            throw new UpdatingModelFailed("Updating DebtModel failed due to an IO Error");
        }

        ListIterator<Debt> iter = listedDebts.listIterator();

        while (iter.hasNext()) {
            Debt debt = iter.next();

            // Check loged in user
            if (!debt.getDebtor().equals(userLogedIn) && !debt.getCreditor().equals(userLogedIn)) {
                iter.remove();
            }

            // Check direction
            if (!direction.equals(DebtDirection.UNSET) && getDirection(debt).equals(direction)) {
                iter.remove();
            }

            // Check fulfilled
            switch (fulfilled) {
                case FULFILLED:
                    if (!debt.isFulfilled()) {
                        iter.remove();
                    }
                    break;
                case NOT_FULFILLED:
                    if (debt.isFulfilled()) {
                        iter.remove();
                    }
                    break;
                default:
                    break;
            }

            // Check user
            if (user != null && !debt.getDebtor().equals(user) && !debt.getCreditor().equals(user)) {
                iter.remove();
            }
        }

        debtListModel = new DebtListModel(listedDebts);

        this.filterDirection = direction;
        this.filterIsFulfilled = fulfilled;
        this.fitleredUser = user;
    }

    private void repay(Debt debt, double amount, LocalDate date) throws DeptPaymentFailedException {
        CashFlow debtorsCashFlow = null;
        CashFlow creditorsCashFlow = null;
        try { // Creates and appends the creaditor's and the debtor's cashflows to the
              // cashflows file
            debtorsCashFlow = cashFlowController.addNewCashFlow(
                    debt.getDebtor(),
                    date,
                    -amount,
                    debt.getDebtAmount().getCurrency(),
                    "Repayed Debt: " + debt.getId());

            creditorsCashFlow = cashFlowController.addNewCashFlow(
                    debt.getCreditor(),
                    date,
                    amount,
                    Currency.getInstance("HUF"),
                    "Repayed Debt: " + debt.getId());
        } catch (InvalidReasonException | CreatingRecordFailed e) {
            throw new DeptPaymentFailedException("Cashflow could not register. Debt payment failed", debt, amount);
        }

        try { // Creates and appends the new payment to the debt
            Payment payment = new Payment(date, debt, new Money(amount, Currency.getInstance("HUF")), debtorsCashFlow,
                    creditorsCashFlow);
            debt.getPayments().add(payment);
            modelSerializer.changeData(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeptPaymentFailedException("Dept payment failed due to an IO Error", debt, amount);
        }
    }

    private User findCounterParty(String name) throws UserNotFound {
        return userController.findUser(name);
    }

    private Debt getSelectedItem(JList<? extends Debt> debtsList) throws NoItemWasSelected {
        Debt selected = debtsList.getSelectedValue();
        if (selected == null) {
            throw new NoItemWasSelected("One debt needs to be selected to Edit");
        }

        return selected;
    }

    private User getDebtor(User counterParty, DebtDirection direction) throws UnknownDebtDirection {
        switch (direction) {
            case I_OWE:
                return userLogedIn;
            case THEY_OWE:
                return counterParty;
            default:
                throw new UnknownDebtDirection("Debt direction is unkonw", direction);
        }

    }

    private User getCreditor(User counterParty, DebtDirection direction) throws UnknownDebtDirection {
        switch (direction) {
            case I_OWE:
                return counterParty;
            case THEY_OWE:
                return userLogedIn;
            default:
                throw new UnknownDebtDirection("Debt direction is unkonw", direction);
        }
    }

    public DebtDirection getDirection(Debt debt) {
        if (debt.getDebtor().equals(userLogedIn)) {
            return DebtDirection.I_OWE;
        }

        return DebtDirection.THEY_OWE;
    }

    // VALIDATORS
    private void validateAmount(double amount, Debt debt) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount, "Amount must be greater than 0");
        }

        if (debt != null && amount <= Debt.repayed(debt).getAmount()) {
            throw new InvalidAmountException(amount, "Amount must stay greater than what's already repayed");
        }
    }

    private boolean paymentAmountIsInvalid(double amount) {
        return amount <= 0.0;
    }

    public enum DebtDirection {
        UNSET,
        I_OWE,
        THEY_OWE
    }

    public enum DebtFulfilled {
        ALL,
        NOT_FULFILLED,
        FULFILLED
    }

}
