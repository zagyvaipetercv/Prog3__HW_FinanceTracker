package financetracker;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import financetracker.controllers.CashFlowController;
import financetracker.controllers.PurchaseController;
import financetracker.controllers.UserController;
import financetracker.exceptions.controller.*;
import financetracker.exceptions.moneycontroller.BalanceCouldNotCahcngeException;
import financetracker.exceptions.moneycontroller.MoneyAmountIsInvalidException;
import financetracker.exceptions.moneycontroller.ReasonIsInvalidException;
import financetracker.models.CashFlow;
import financetracker.models.Money;
import financetracker.models.User;
import financetracker.views.AddMoneyView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MainFrame;

public class Main {
    private static CashFlowController cashFlowController;
    private static UserController userController;

    public static void main(String[] args) {
        try {
            cashFlowController = new CashFlowController();
            userController = new UserController();
        } catch (CannotCreateControllerException e) {
            ErrorBox.show("ERROR", e.getMessage());
            System.exit(-1);
        }

        // TODO: Uncomment the next line of code
        // userController.getLoginView().setVisible(true);

        // TODO: Remove code after testing
        User testUser = new User(-1, "Test", "Password");
        MainFrame mainFrame = new MainFrame(testUser);
        mainFrame.setVisible(true);
    }

    public static CashFlowController getCashFlowController() {
        return cashFlowController;
    }

    public static UserController getUserController() {
        return userController;
    }
}