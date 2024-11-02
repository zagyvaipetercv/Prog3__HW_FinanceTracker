package financetracker.exceptions.controller;

import financetracker.controllers.Controller;
import financetracker.models.Model;

public class ControllerCannotReadException extends Exception {
    private final transient Controller<? extends Model> controller;

    public ControllerCannotReadException(Controller<? extends Model> controller) {
        this(controller, controller.getClass().toString() + " can't read data from " + controller.getFilePath());
    }

    public ControllerCannotReadException(Controller<? extends Model> controller, String message) {
        super(message);
        this.controller = controller;
    }

    public Controller<? extends Model> getController() {
        return controller;
    }
}
