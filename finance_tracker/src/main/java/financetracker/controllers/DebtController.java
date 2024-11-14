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
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.debtcontroller.CreatingDebtFailedException;
import financetracker.exceptions.debtcontroller.EditingDebtFailedException;
import financetracker.exceptions.debtcontroller.FulfilledDebtCantChange;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.models.DebtListModel;
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

    public FrameView getAddPaymentView(JList<? extends Debt> debtList) throws NoItemWasSelected, FulfilledDebtCantChange {
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
    }

    public DebtController(MainFrame mainFrame) throws ControllerWasNotCreated {
        this(DEFAULT_SAVE_PATH, mainFrame);
    }

    private void refreshDebtView() {
        mainFrame.changeView(getDebtView());
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

        Debt debt = new Debt(id, counterParty, direction, date, money, new ArrayList<>(), false, hasDeadline,
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

        Debt changedDebt = new Debt(debt.getId(), counterParty, direction, date, money, debt.getPayments(),
                debt.isFulfilled(), hasDeadline, (hasDeadline ? deadline : null));

        try {
            modelSerializer.changeData(changedDebt);
            int idx = debtListModel.indexOf(debt);
            debtListModel.removeElement(debt);
            debtListModel.add(idx, changedDebt);
            cachedDebts.remove(debt);
            cachedDebts.add(changedDebt);

        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new EditingDebtFailedException("Debt was not edited due to an IO Error", debt, changedDebt);
        }

        refreshDebtView();
    }

    public Money repayed(Debt debt) {
        double amount = 0.0;
        for (Payment payment : debt.getPayments()) {
            amount += payment.getAmount().getAmount();
        }

        return new Money(amount, Currency.getInstance("HUF"));
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
}
