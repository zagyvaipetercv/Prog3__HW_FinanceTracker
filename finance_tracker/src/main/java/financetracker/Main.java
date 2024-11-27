package financetracker;

import financetracker.controllers.UserController;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.MainFrame;

public class Main {
    /**
     * Opens Login window where the user can register or login
     * @param args never used
     */
    public static void main(String[] args) {
        MainFrame mainFrame = null;
        try {
            UserController userController = new UserController(mainFrame);
            userController.getLoginView().setVisible(true);
        } catch (ControllerWasNotCreated e) {
            ErrorBox.show(null, e.getErrorTitle(), e.getMessage());
            System.exit(-1);
        }
    }
}