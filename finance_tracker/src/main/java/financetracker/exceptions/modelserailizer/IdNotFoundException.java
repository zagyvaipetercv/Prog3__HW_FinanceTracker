package financetracker.exceptions.modelserailizer;

import financetracker.datatypes.Model;

public class IdNotFoundException extends Exception {
    private  final transient Model model;

    public IdNotFoundException(Model model, String message) {
        super(message);

        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
