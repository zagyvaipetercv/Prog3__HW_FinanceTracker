package financetracker.exceptions.generic;

import financetracker.datatypes.Model;
import financetracker.exceptions.ErrorBoxException;

public class DeletingRecordFailed extends ErrorBoxException {
    private static final String ERROR_TITLE = "DELETING RECORD FAILED";

    private final transient Model model;

    public DeletingRecordFailed(String message, Model model) {
        super(message);
        this.model = model;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Model getModel() {
        return model;
    }

}
