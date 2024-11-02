package financetracker.exceptions.controller;

import financetracker.models.Model;

public class IdNotFoundException extends Exception {
    private final transient Model model;

    public IdNotFoundException(Model model) {
        this(model, "Id Not found for model");
    }

    public IdNotFoundException(Model model, String message) {
        super(message);
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
