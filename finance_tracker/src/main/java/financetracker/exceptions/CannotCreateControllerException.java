package financetracker.exceptions;

import financetracker.controllers.Controller;

public class CannotCreateControllerException extends Exception {
    private final transient Class<? extends Controller> controllerClass;

    public CannotCreateControllerException(Class<? extends Controller> controllerClass, String reason) {
        super("Couldn't create UserController - " + reason);
        this.controllerClass = controllerClass;
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }
}
