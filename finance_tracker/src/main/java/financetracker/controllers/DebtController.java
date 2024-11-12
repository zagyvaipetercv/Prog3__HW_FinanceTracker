package financetracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.datatypes.Debt;
import financetracker.datatypes.Money;
import financetracker.datatypes.Payment;
import financetracker.datatypes.User;
import financetracker.datatypes.Debt.DebtDirection;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.debtcontroller.CreatingDebtFailedException;
import financetracker.exceptions.modelserailizer.SerializerCannotRead;
import financetracker.exceptions.modelserailizer.SerializerCannotWrite;
import financetracker.exceptions.usercontroller.UserNotFound;
import financetracker.models.DebtListModel;
import financetracker.views.base.FrameView;
import financetracker.views.base.PanelView;
import financetracker.views.debt.AddNewDebtView;
import financetracker.views.debt.DebtView;
import financetracker.windowing.MainFrame;

public class DebtController extends Controller<Debt> {
    private static final String DEFAULT_SAVE_PATH = "saves\\debt.dat";

    private DebtListModel debtListModel;

    private List<Debt> cachedDebts;

    private UserController userController;

    public FrameView getAddNewDebtView() {
        return new AddNewDebtView(this);
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

    public void addDebt(String name, DebtDirection direction, LocalDate date, String amountString, boolean hasDeadline, LocalDate deadline) throws UserNotFound, InvalidAmountException, CreatingDebtFailedException {
        long id = modelSerializer.getNextId();
        User counterParty = findCounterParty(name);
        double amount = Money.parseAmount(amountString);
        if (!isMoneyAmountValid(amount)) {
            throw new InvalidAmountException(amountString, "Amount must be greater than 0");
        }

        Money money = new Money(amount, Currency.getInstance("HUF"));

        if (hasDeadline) {
            deadline = null;
        }
        
        Debt debt = new Debt(id, counterParty, direction, date, money, new ArrayList<Payment>(), false, deadline);
        try {
            modelSerializer.appendNewData(debt);
            debtListModel.addElement(debt);
            cachedDebts.add(debt);
        } catch (SerializerCannotRead | SerializerCannotWrite e) {
            throw new CreatingDebtFailedException(debt, "Creating debt failed due to an IO Error");
        }
    }

    public Money repayed(Debt debt) {
        double amount = 0.0;
        for (Payment payment : debt.getPayments()) {
            amount += payment.getAmount().getAmount();
        }

        return new Money(amount, Currency.getInstance("HUF"));
    }

    public User findCounterParty(String name) throws UserNotFound {
        User user = userController.findUser(name);
        return user;
    }

    private boolean isMoneyAmountValid(double amount) {
        return amount > 0.0;
    }
}
