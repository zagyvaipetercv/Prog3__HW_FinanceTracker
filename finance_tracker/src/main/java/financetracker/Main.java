package financetracker;

import financetracker.controllers.UserController;
import financetracker.exceptions.controller.*;
import financetracker.windowing.ErrorBox;

public class Main {
    public static void main(String[] args) {
        UserController userController;
        try {
            userController = new UserController();
            userController.getLoginView();
        } catch (CannotCreateControllerException e) {
            ErrorBox.show("ERROR", e.getMessage());
            System.exit(-1);
        }
    }
}