package financetracker;

import financetracker.controllers.MoneyController;
import financetracker.controllers.UserController;
import financetracker.exceptions.controller.*;
import financetracker.views.AddMoneyView;
import financetracker.windowing.ErrorBox;

public class Main {
    public static void main(String[] args) {
        /*
        UserController userController;
        try {
            userController = new UserController();
            userController.getLoginView();
        } catch (CannotCreateControllerException e) {
            ErrorBox.show("ERROR", e.getMessage());
            System.exit(-1);
        }
        */

        // TODO: remove this adter testing
        MoneyController moneyController;
        try {
            moneyController = new MoneyController();
            new AddMoneyView(moneyController);
        } catch (CannotCreateControllerException e) {
            e.printStackTrace();
        }
    }
}