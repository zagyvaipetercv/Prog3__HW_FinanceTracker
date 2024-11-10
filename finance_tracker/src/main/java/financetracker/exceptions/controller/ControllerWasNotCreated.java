package financetracker.exceptions.controller;

import financetracker.controllers.Controller;
import financetracker.exceptions.ErrorBoxException;

public class ControllerWasNotCreated extends ErrorBoxException {

    private static final String ERROR_TITLE = "Controller was not created"; 
    private final transient Class<? extends Controller> controllerClass; 

    public ControllerWasNotCreated(String message, Class<? extends Controller> controllerClass) {
        super(message);
        this.controllerClass = controllerClass;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }
}
