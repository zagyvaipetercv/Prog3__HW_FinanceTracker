package financetracker.exceptions.category;

import financetracker.exceptions.ErrorBoxException;

public class CategoryLookupFailedException extends ErrorBoxException {
    private static final String ERROR_TITLE = "CATEGORY LOOKUP FAILED";

    
    public CategoryLookupFailedException(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
