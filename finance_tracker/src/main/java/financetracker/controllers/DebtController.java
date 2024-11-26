package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

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
    /**
     * Returns an updated filtered DebtView where you can check the debts of the
     * signed in user
     * 
     * @return an updateded, filtered DebtView
     * @throws UpdatingModelFailed if the DebtListModel faield to update
     */
    public PanelView getDebtView() throws UpdatingModelFailed {
        updateDebtListModel(filterDirection, filterIsFulfilled, fitleredUser);
        return new DebtView(this, debtListModel,
                filterDirection, filterIsFulfilled, fitleredUser);
    }

    /**
     * Returns an EditSelectedDebtView frame filled with the selected debt's
     * properties where you can change the selected debt's properties
     * 
     * @param debt the debt which will be edited
     * @return an EditSelectedDebtView
     * @throws NoItemWasSelected       if no item from the lsit was selected
     * @throws FulfilledDebtCantChange if the debt is already filfilled
     */
    public FrameView getEditSelectedDebtView(Debt debt)
            throws NoItemWasSelected, FulfilledDebtCantChange {

        if (debt == null) {
            throw new NoItemWasSelected("A debt must be selected to edit");
        }

        if (debt.isFulfilled()) {
            throw new FulfilledDebtCantChange("Can't edit a debt that is already fulfilled", debt);
        }

        return new EditSelectedDebtView(this, debt);
    }

    /**
     * Returns an AddNewDebtView frame where you can add and save a new debt
     * 
     * @return an AddNewDebtView frame
     */
    public FrameView getAddNewDebtView() {
        return new AddNewDebtView(this);
    }

    /**
     * Returns an AddPaymentView for the selected debt
     * 
     * @param debt the debt which will be payed
     * @return an AddPaymentView for the selected debt
     * @throws NoItemWasSelected       no debt was selected
     * @throws FulfilledDebtCantChange if the debt is already fulfilled
     */
    public FrameView getAddPaymentView(Debt debt)
            throws NoItemWasSelected, FulfilledDebtCantChange {
        if (debt == null) {
            throw new NoItemWasSelected("A debt must be selected to edit");
        }

        if (debt.isFulfilled()) {
            throw new FulfilledDebtCantChange("Can't repay an already fulfilled debt", debt);
        }

        return new AddPaymentView(this, debt);
    }

    /*
     * Refreshes the debt view by creating a new instance and updating the main
     * panel of mainFrame
     */
    public void refreshDebtView() throws UpdatingModelFailed {
        updateDebtListModel(filterDirection, filterIsFulfilled, fitleredUser);
        mainFrame.changeView(getDebtView());
    }

    // ACTIONS

    /**
     * Creates and saves a new debt with 0 payment.
     * 
     * @param name         the other party's name
     * @param direction    direction of debt
     *                     <ul>
     *                     <li>I_OWE - user signed in is the debtor, the other party
     *                     is the creditor</li>
     *                     <li>THEY_OWE - user signed in is the creditor, the other
     *                     party is the debtor</li>
     *                     <li>UNSET - (default) the debt dirextion is not set
     *                     yet</li>
     *                     </ul>
     * @param date         date of the debt
     * @param amountString a string representing the amount of the debt
     * @param hasDeadline
     *                     <ul>
     *                     <li>true - if the debt has a deadline</li>
     *                     <li>flase - if the debt has no deadline</li>
     *                     <ul>
     * @param deadline     date of the deadline
     * @throws UserNotFound           if no user with the specified name was found
     * @throws InvalidAmountException if the amount was less or equal to 0 or amount
     *                                could not be parsed to a double value
     * @throws CreatingRecordFailed   if an IO Error occured or DebtDirection was
     *                                UNSET
     */
    public void addDebt(String name, DebtDirection direction, LocalDate date, String amountString, boolean hasDeadline,
            LocalDate deadline) throws UserNotFound, InvalidAmountException, CreatingRecordFailed {
        long id = modelSerializer.getNextId();
        User counterParty = userController.findUser(name);
        double amount = Money.parseAmount(amountString);

        validateAmount(amount, null);

        Money money = new Money(amount, Currency.getInstance("HUF"));
        if (counterParty.equals(userLogedIn)) {
            throw new CreatingRecordFailed("Other party can't be the user signed in", null);
        }

        User debtor;
        User creditor;
        try {
            debtor = calculateDebtor(counterParty, direction);
            creditor = calculateCreditor(counterParty, direction);
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

    /**
     * Edits a debt specified in the parameters.
     * <p>
     * Only the date, the amount and the deadline can be changed, because changeing
     * the direction would impact others cashflows too.
     * 
     * @param debt         debt that will be edited
     * @param date         the new date value
     * @param amountString a string representing the new value
     * @param hasDeadline  true if debt should have a deadline, false if not
     * @param deadline     the new value of the deadline
     * @throws InvalidAmountException
     *                                <ul>
     *                                <li>if the amount can't be parsed to a double
     *                                value</li>
     *                                <li>if the amount is 0 or less</li>
     *                                <li>if the amount is less than what's already
     *                                payed</li>
     *                                </ul>
     * @throws EditingRecordFailed    if editing the debt failed due to an IO Error
     */
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

    /**
     * Removes a debt from the save files
     * 
     * @param debt the debt that needs to be removed
     * @throws DeletingRecordFailed if the debt was not deleted due to an IO Error.
     * @throws NoItemWasSelected if no debt was selected
     */
    public void deleteDebt(Debt debt) throws DeletingRecordFailed, NoItemWasSelected {
        if (debt == null) {
            throw new NoItemWasSelected("A debt must be selected to delete");
        }

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

    /**
     * Adds and saves new payment for a debt specified in the parameters.
     * 
     * @param debt         the debt that will be payed
     * @param amountString the string representation of the amount of the payment
     * @param date         date of the payment
     * @throws InvalidAmountException        if the amount can't be parsed to a
     *                                       double or the amount is 0 or less
     * @throws DeptPaymentFailedException    if an IO Error occured during the
     *                                       process
     * @throws PaymentIsGreaterThanRemaining if the payed amount is greater than the
     *                                       remaining (the remaining amount will be
     *                                       payed)
     */
    public void payDebt(Debt debt, String amountString, LocalDate date)
            throws InvalidAmountException, DeptPaymentFailedException, PaymentIsGreaterThanRemaining {

        double amount = Money.parseAmount(amountString);

        double remainingDebtAmount = debt.getDebtAmount().getAmount() - debt.getPayedAmount().getAmount();

        validateAmount(amount, null);

        if (CustomMath.almostEquals(amount, remainingDebtAmount)) {
            amount = remainingDebtAmount;
        }

        if (remainingDebtAmount < amount) {
            pay(debt, remainingDebtAmount, date);
            throw new PaymentIsGreaterThanRemaining(
                    "Payed amount is greater than remaining. Only remaining will be payed",
                    new Money(remainingDebtAmount, Currency.getInstance("HUF")),
                    new Money(amount, Currency.getInstance("HUF")));
        }

        pay(debt, amount, date);
    }

    /**
     * Pays the full remaining amount for the debt
     * 
     * @param debt the debt which will be payed
     * @param date date of payment
     * @throws DeptPaymentFailedException if an IO Error occured
     */
    public void payAll(Debt debt, LocalDate date) throws DeptPaymentFailedException {
        double remaining = debt.getDebtAmount().getAmount() - debt.getPayedAmount().getAmount();

        pay(debt, remaining, date);
    }

    /**
     * Filters for a specified direction, user and fulfilled state. Then updates the
     * DebtListModel according to the filters.
     * 
     * @param direction direction of debt
     *                  *
     *                  <ul>
     *                  <li>I_OWE - show only those where the user singed in is the
     *                  debtor</li>
     *                  <li>THEY_OWE - show only those where the user signed in is
     *                  the creditor</li>
     *                  <li>UNSET - show all</li>
     *                  </ul>
     * @param fulfilled
     *                  <ul>
     *                  <li>ALL - show all</li>
     *                  <li>FULFILLED - show only those where the debt is
     *                  fulfilled</li>
     *                  <li>NOT_FULFILLED - show only those where the debt is not
     *                  fulfilled</li>
     *                  </ul>
     * @param userName  the name of the user that is either the debtor or the
     *                  creditor. If left empty then the function wont filter for
     *                  username.
     * @throws UserNotFound        if the user with the specified name was not found
     * @throws UpdatingModelFailed if updateing the model failed due to an IO Error
     */
    public void filterFor(DebtDirection direction, DebtFulfilled fulfilled, String userName)
            throws UserNotFound, UpdatingModelFailed {

        User user = null;
        if (!userName.isEmpty()) {
            user = userController.findUser(userName);
        }
        updateDebtListModel(direction, fulfilled, user);

        this.filterDirection = direction;
        this.filterIsFulfilled = fulfilled;
        this.fitleredUser = user;
    }

    // HELPER METHODS

    /**
     * Updates the DebtListModel based on a specifeid debt direction, a user and a
     * fulfilled state
     * 
     * @param direction
     *                  <ul>
     *                  <li>UNSET - model will not fitler for direction type</li>
     *                  <li>I_OWE - model will only contain records where the user
     *                  signed in is the debtor</li>
     *                  <li>THEY_OWE - model will only contain records where the
     *                  user signed in is the creditor</li>
     *                  </ul>
     * @param fulfilled
     *                  <ul>
     *                  <li>ALL - model will not fitler for fulfilled state</li>
     *                  <li>FULFILLED - model will only contain records where the
     *                  debt is fulfilled</li>
     *                  <li>NOT_FULFILLED- model will only contain records where the
     *                  debt is not fulfilled</li>
     *                  </ul>
     * @param user      model will only contain records where the user is either the
     *                  debtor or the creditor. If set to null then the model will
     *                  not be filtered for a user.
     * @throws UpdatingModelFailed if updateing the model failed due to an IO Error
     */
    private void updateDebtListModel(DebtDirection direction, DebtFulfilled fulfilled, User user)
            throws UpdatingModelFailed {
        List<Debt> listedDebts = new ArrayList<>();

        try {
            for (Debt debt : modelSerializer.readAll()) {
                // Check loged in user
                if (!debt.getDebtor().equals(userLogedIn) &&
                        !debt.getCreditor().equals(userLogedIn)) {
                    continue;
                }

                // Check direction
                if (!direction.equals(DebtDirection.UNSET) && !getDirection(debt).equals(direction)) {
                    continue;
                }

                // Check fulfilled
                switch (fulfilled) {
                    case FULFILLED:
                        if (!debt.isFulfilled()) {
                            continue;
                        }
                        break;
                    case NOT_FULFILLED:
                        if (debt.isFulfilled()) {
                            continue;
                        }
                        break;
                    default:
                        break;
                }

                // Check user
                if (user != null && !debt.getDebtor().equals(user) && !debt.getCreditor().equals(user)) {
                    continue;
                }

                listedDebts.add(debt);
            }
        } catch (SerializerCannotRead e) {
            throw new UpdatingModelFailed("Updating DebtListModel failed due to an IO Error");
        }

        debtListModel = new DebtListModel(listedDebts);
    }

    /**
     * Payes a specified amount to a debt.
     * 
     * @param debt   the debt which will be payed
     * @param amount the amount that will be added to the debt's payment
     * @param date   the date of the payment
     * @throws DeptPaymentFailedException if the payment failed due to an IO Error
     */
    private void pay(Debt debt, double amount, LocalDate date) throws DeptPaymentFailedException {
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

    /**
     * Calculates and returns the debtor (User) between a given user and the user
     * signed in based on the debt direction
     * 
     * @param counterParty the other user
     * @param direction    the direction of the payment
     * @return the user that should be the debtor
     * @throws UnknownDebtDirection if the direction is not set to I_OWE or THEY_OWE
     */
    private User calculateDebtor(User counterParty, DebtDirection direction) throws UnknownDebtDirection {
        switch (direction) {
            case I_OWE:
                return userLogedIn;
            case THEY_OWE:
                return counterParty;
            default:
                throw new UnknownDebtDirection("Debt direction is unkonw", direction);
        }

    }

    /**
     * Calculates and returns the creditor (User) between a given user and the user
     * signed in based on the debt direction
     * 
     * @param counterParty the other user
     * @param direction    the direction of the payment
     * @return the user that should be the creditor
     * @throws UnknownDebtDirection if the direction is not set to I_OWE or THEY_OWE
     */
    private User calculateCreditor(User counterParty, DebtDirection direction) throws UnknownDebtDirection {
        switch (direction) {
            case I_OWE:
                return counterParty;
            case THEY_OWE:
                return userLogedIn;
            default:
                throw new UnknownDebtDirection("Debt direction is unkonw", direction);
        }
    }

    /**
     * Returns the direction of a debt based on the current user signed in
     * 
     * @param debt
     * @return
     *         <ul>
     *         <li>I_OWE - if the user signed in is the debtor</li>
     *         <li>THEY_OWE - if the user signed in is the creditor</li>
     *         </ul>
     */
    public DebtDirection getDirection(Debt debt) {
        if (debt.getDebtor().equals(userLogedIn)) {
            return DebtDirection.I_OWE;
        }

        return DebtDirection.THEY_OWE;
    }

    // VALIDATORS
    /**
     * Checks if the amount is valid. Throws exception if not.
     * <p>
     * Amount must be greater than 0 and greater than the repayed amount of the debt
     * if it was not set to null
     * 
     * @param amount the amount that will be checked
     * @param debt   the debt to compare with (can be setto null)
     * @throws InvalidAmountException if one of the checks fail
     */
    private void validateAmount(double amount, Debt debt) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount, "Amount must be greater than 0");
        }

        if (debt != null && amount <= debt.getPayedAmount().getAmount()) {
            throw new InvalidAmountException(amount, "Amount must stay greater than what's already repayed");
        }
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

    // FOR-TESTING
    public List<Debt> getAllDebts() throws SerializerCannotRead {
        return modelSerializer.readAll();
    }
}
