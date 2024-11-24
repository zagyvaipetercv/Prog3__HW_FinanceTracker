package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.JList;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.datatypes.Payment;
import financetracker.datatypes.User;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotChangeException;
import financetracker.exceptions.cashflowcontroller.DeletingCashFlowFailed;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.debtcontroller.CreatingDebtFailedException;
import financetracker.exceptions.debtcontroller.DeletingDebtFailedException;
import financetracker.exceptions.debtcontroller.DeptPaymentFailedException;
import financetracker.exceptions.debtcontroller.EditingDebtFailedException;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.exceptions.debtcontroller.PaymentIsGreaterThanRemaining;
import financetracker.exceptions.debtcontroller.UnknownDebtDirection;
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

    private DebtListModel debtListModel;

    private List<Debt> cachedDebts;
    private List<Debt> shownDebts;

    private UserController userController;
    private CashFlowController cashFlowController;

    private User userLogedIn;

    // CONSTRUCTORS
    public DebtController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);

        userLogedIn = mainFrame.getUserLogedIn();

        try {
            reloadCache();
        } catch (SerializerCannotRead e) {
            throw new ControllerWasNotCreated("Couldn't read saves file", this.getClass());
        }

        shownDebts = new ArrayList<>(cachedDebts);

        debtListModel = new DebtListModel(shownDebts);
        userController = mainFrame.getUserController();
        cashFlowController = mainFrame.getCashFlowController();
    }

    public DebtController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    // FRAMEVIEW GETTERS
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

    public PanelView getDebtView() {
        return new DebtView(this, debtListModel);
    }

    public void refreshDebtView() {
        mainFrame.changeView(getDebtView());
    }

    // ACTIONS
    public void addDebt(String name, DebtDirection direction, LocalDate date, String amountString, boolean hasDeadline,
            LocalDate deadline) throws UserNotFound, InvalidAmountException, CreatingDebtFailedException {
        long id = modelSerializer.getNextId();
        User counterParty = findCounterParty(name);
        double amount = Money.parseAmount(amountString);

        validateAmount(amount, null);

        Money money = new Money(amount, Currency.getInstance("HUF"));
        if (counterParty.equals(userLogedIn)) {
            throw new CreatingDebtFailedException(null, "Other party can't be the user");
        }

        User debtor;
        User creditor;
        try {
            debtor = getDebtor(counterParty, direction);
            creditor = getCreditor(counterParty, direction);
        } catch (UnknownDebtDirection e) {
            throw new CreatingDebtFailedException(null, "Creating debt failed due to unkonw direction");
        }

        Debt debt = new Debt(id, debtor, creditor, date, money, new ArrayList<>(),
                (hasDeadline ? deadline : null));
        try {
            modelSerializer.appendNewData(debt);
            debtListModel.addElement(debt);
            cachedDebts.add(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingDebtFailedException(debt, "Creating debt failed due to an IO Error");
        }
    }

    public void editDebt(Debt debt, LocalDate date, String amountString, boolean hasDeadline, LocalDate deadline)
            throws InvalidAmountException, EditingDebtFailedException {

        double amount = Money.parseAmount(amountString);
        validateAmount(amount, debt);

        Money money = new Money(amount, Currency.getInstance("HUF"));

        debt.setDate(date);
        debt.setDebtAmount(money);
        debt.setDeadline((hasDeadline ? deadline : null));

        try {
            replaceDebtEverywhere(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingDebtFailedException("Debt was not edited due to an IO Error", debt);
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

        if (amount > remainingDebtAmount) {
            throw new PaymentIsGreaterThanRemaining(
                    "Payment is greater than the remaining amount",
                    new Money(remainingDebtAmount, Currency.getInstance("HUF")),
                    new Money(amount, Currency.getInstance("HUF")));
        }

        repay(debt, amount, date);
    }

    public void repayAll(Debt debt, LocalDate date) throws DeptPaymentFailedException {
        double remaining = debt.getDebtAmount().getAmount() - Debt.repayed(debt).getAmount();

        repay(debt, remaining, date);
    }

    public void filterFor(DebtDirection direction, DebtFulfilled fulfilled, String userName) throws UserNotFound {
        shownDebts = new ArrayList<>(cachedDebts);

        for (Debt debt : cachedDebts) {
            // Check direction
            if (!direction.equals(DebtDirection.UNSET) && getDirection(debt).equals(direction)) {
                shownDebts.remove(debt);
            }

            // Check fulfilled
            switch (fulfilled) {
                case FULFILLED:
                    if (!debt.isFulfilled()) {
                        shownDebts.remove(debt);
                    }
                    break;
                case NOT_FULFILLED:
                    if (debt.isFulfilled()) {
                        shownDebts.remove(debt);
                    }
                    break;
                default:
                    break;
            }

            // Check user
            if (!userName.isBlank()) {
                User counterParty = userController.findUser(userName);
                User debtor = debt.getDebtor();
                User creditor = debt.getCreditor();
                if (!counterParty.equals(debtor) && !counterParty.equals(creditor)) {
                    shownDebts.remove(debt);
                }
            }
        }

        debtListModel = new DebtListModel(shownDebts);
        refreshDebtView();
    }

    public void deleteDebt(Debt debt) throws DeletingDebtFailedException {
        try {
            removeDebtEverywhere(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeletingDebtFailedException("Debt was not removed due to an IO Error", debt);
        } catch (DeletingCashFlowFailed e) {
            throw new DeletingDebtFailedException("Debt could not delete cashflows", debt);
        }
    }

    // HELPER METHODS
    private void repay(Debt debt, double amount, LocalDate date) throws DeptPaymentFailedException {
        try {
            CashFlow debtors = null;
            CashFlow creditors = null;
            try {
                debtors = cashFlowController.addNewCashFlow(
                        debt.getDebtor(),
                        date,
                        -amount,
                        debt.getDebtAmount().getCurrency(),
                        "Repayed Debt: " + debt.getId());

                creditors = cashFlowController.addNewCashFlow(
                        debt.getCreditor(),
                        date,
                        amount,
                        Currency.getInstance("HUF"),
                        "Repayed Debt: " + debt.getId());
            } catch (InvalidReasonException | BalanceCouldNotChangeException e) {
                throw new DeptPaymentFailedException("Cashflow could not register. Debt payment failed", debt, amount);
            }

            Payment payment = new Payment(date, debt, new Money(amount, Currency.getInstance("HUF")), debtors,
                    creditors);
            debt.getPayments().add(payment);
            replaceDebtEverywhere(debt);

        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeptPaymentFailedException("Dept payment failed due to an IO Error", debt, amount);
        }
    }

    private User findCounterParty(String name) throws UserNotFound {
        return userController.findUser(name);
    }

    private void validateAmount(double amount, Debt debt) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount, "Amount must be greater than 0");
        }

        if (debt != null && amount <= Debt.repayed(debt).getAmount()) {
            throw new InvalidAmountException(amount, "Amount must stay greater than what's already repayed");
        }
    }

    private Debt getSelectedItem(JList<? extends Debt> debtsList) throws NoItemWasSelected {
        Debt selected = debtsList.getSelectedValue();
        if (selected == null) {
            throw new NoItemWasSelected("One debt needs to be selected to Edit");
        }

        return selected;
    }

    private void replaceDebtEverywhere(Debt replaced) throws SerializerCannotRead, SerializerCannotWrite {
        modelSerializer.changeData(replaced);
        int idx = debtListModel.indexOf(replaced);
        debtListModel.removeElement(replaced);
        debtListModel.add(idx, replaced);
        cachedDebts.remove(replaced);
        cachedDebts.add(replaced);
    }

    private void removeDebtEverywhere(Debt debt)
            throws SerializerCannotRead, SerializerCannotWrite, DeletingCashFlowFailed {
        long id = debt.getId();
        int idx = debtListModel.indexOf(debt);

        for (Payment payment : debt.getPayments()) {
            cashFlowController.deleteCashFlow(payment.getDebtorsCashFlow());
            cashFlowController.deleteCashFlow(payment.getCreditorsCashFlow());
        }
        modelSerializer.removeData(id);
        debtListModel.remove(idx);
        cachedDebts.remove(debt);
    }

    private boolean paymentAmountIsInvalid(double amount) {
        return amount <= 0.0;
    }

    private void reloadCache() throws SerializerCannotRead {
        List<Debt> all = modelSerializer.readAll();
        cachedDebts = new ArrayList<>();
        for (Debt debt : all) {
            if (debt.getDebtor().getId() == userLogedIn.getId() || debt.getCreditor().getId() == userLogedIn.getId()) {
                cachedDebts.add(debt);
            }
        }
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
        if (debt.getDebtor().getId() == userLogedIn.getId()) {
            return DebtDirection.I_OWE;
        }

        return DebtDirection.THEY_OWE;
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
