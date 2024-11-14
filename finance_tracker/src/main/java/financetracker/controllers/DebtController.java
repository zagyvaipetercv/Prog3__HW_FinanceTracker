package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.JList;

import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.datatypes.Payment;
import financetracker.datatypes.User;
import financetracker.datatypes.Debt.DebtDirection;
import financetracker.exceptions.NoItemWasSelected;
import financetracker.exceptions.cashflowcontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.cashflowcontroller.InvalidReasonException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.debtcontroller.CreatingDebtFailedException;
import financetracker.exceptions.debtcontroller.DeptPaymentFailedException;
import financetracker.exceptions.debtcontroller.EditingDebtFailedException;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.exceptions.debtcontroller.PaymentIsGreaterThanRemaining;
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

    private UserController userController;
    private CashFlowController cashFlowController;

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

    public DebtController(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        super(saveFilePath, mainFrame);

        try {
            cachedDebts = modelSerializer.readAll();
        } catch (SerializerCannotRead e) {
            throw new ControllerWasNotCreated("Couldn't read saves file", this.getClass());
        }
        debtListModel = new DebtListModel(cachedDebts);
        userController = new UserController(mainFrame);
        cashFlowController = new CashFlowController(mainFrame);
    }

    public DebtController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    public void refreshDebtView() {
        mainFrame.changeView(getDebtView());
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

    private void repay(Debt debt, double amount, LocalDate date) throws DeptPaymentFailedException {
        Payment payment = new Payment(date, debt, new Money(amount, Currency.getInstance("HUF")));
        debt.getPayments().add(payment);
        
        String stringAmount;
        switch (debt.getDirection()) {
            case I_OWE:
                stringAmount = Double.toString(-amount);
                break;

            case THEY_OWE:
                stringAmount = Double.toString(amount);
                break;

            default: 
                stringAmount = ""; // Will fail cashflow
                break;
        }
        
        try {
            replaceDebtEverywhere(debt);
            
            cashFlowController.changeMoneyOnAccount(
                    date,
                    stringAmount,
                    debt.getDebtAmount().getCurrency(),
                    "Repayed Debt: " + debt.getId());
            
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new DeptPaymentFailedException("Dept payment failed due to an IO Error", debt, amount);

        } catch (InvalidReasonException | BalanceCouldNotCahcngeException | InvalidAmountException e) {
            // If cashflow couldn't register -> go back to original
            debt.getPayments().remove(payment);

            try {
                replaceDebtEverywhere(debt);
            } catch (SerializerCannotRead | SerializerCannotWrite e1) {
                throw new DeptPaymentFailedException("Payment was registered in in debt but not in cash flows. Add cashflow manually", debt, amount);
            }   

            throw new DeptPaymentFailedException("Cashflow could not register. Debt payment failed", debt, amount);
        }

    }

    public void addDebt(String name, DebtDirection direction, LocalDate date, String amountString, boolean hasDeadline,
            LocalDate deadline) throws UserNotFound, InvalidAmountException, CreatingDebtFailedException {
        long id = modelSerializer.getNextId();
        User counterParty = findCounterParty(name);
        double amount = Money.parseAmount(amountString);
        if (!isMoneyAmountValid(amount)) {
            throw new InvalidAmountException(amountString, "Amount must be greater than 0");
        }

        Money money = new Money(amount, Currency.getInstance("HUF"));

        Debt debt = new Debt(id, counterParty, direction, date, money, new ArrayList<>(),
                (hasDeadline ? deadline : null));
        try {
            modelSerializer.appendNewData(debt);
            debtListModel.addElement(debt);
            cachedDebts.add(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingDebtFailedException(debt, "Creating debt failed due to an IO Error");
        }
    }

    public void editDebt(Debt debt, String name, DebtDirection direction, LocalDate date, String amountString,
            boolean hasDeadline,
            LocalDate deadline) throws InvalidAmountException, UserNotFound, EditingDebtFailedException {

        User counterParty = findCounterParty(name);
        double amount = Money.parseAmount(amountString);
        if (!isMoneyAmountValid(amount)) {
            throw new InvalidAmountException(amountString, "Amount must be greater than 0");
        }

        Money money = new Money(amount, Currency.getInstance("HUF"));

        Debt newDebt = new Debt(debt.getId(), counterParty, direction, date, money, debt.getPayments(),
                (hasDeadline ? deadline : null));

        debt.setCounterParty(counterParty);
        debt.setDirection(direction);
        debt.setDate(date);
        debt.setDebtAmount(money);
        debt.setDeadline((hasDeadline ? deadline : null));

        try {
            replaceDebtEverywhere(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingDebtFailedException("Debt was not edited due to an IO Error", debt, newDebt);
        }

    }

    public User findCounterParty(String name) throws UserNotFound {
        return userController.findUser(name);
    }

    private boolean isMoneyAmountValid(double amount) {
        return amount > 0.0;
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

    private boolean paymentAmountIsInvalid(double amount) {
        return amount <= 0.0;
    }
}
