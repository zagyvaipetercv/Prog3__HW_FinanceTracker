package financetracker;

import financetracker.controllers.UserController;
import financetracker.exceptions.CannotCreateUserControllerException;
import financetracker.windowing.ErrorBox;

public class Main {
    public static void main(String[] args) {
        UserController userController;
        try {
            userController = new UserController();
            userController.showLoginView();
        } catch (CannotCreateUserControllerException e) {
            ErrorBox.show("ERROR", e.getMessage());
            System.exit(-1);
        }
    }
}