package financetracker.exceptions.controller;

import financetracker.controllers.Controller;
import financetracker.models.Model;

public class ControllerCannotWriteException extends Exception {
    private final transient Controller<? extends Model> controller;

    public ControllerCannotWriteException(Controller<? extends Model> controller) {
        this(controller, controller.getClass().toString() + " can't write data to " + controller.getFilePath());
    }

    public ControllerCannotWriteException(Controller<? extends Model> controller, String message) {
        super(message);
        this.controller = controller;
    }

    public Controller<? extends Model> getController() {
        return controller;
    }
}
