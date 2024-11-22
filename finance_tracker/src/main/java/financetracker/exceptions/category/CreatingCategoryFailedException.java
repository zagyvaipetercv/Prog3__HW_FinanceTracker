package financetracker.exceptions.category;

import financetracker.datatypes.Category;
import financetracker.exceptions.ErrorBoxException;

public class CreatingCategoryFailedException extends ErrorBoxException {
    private static final String ERROR_TITLE = "CREATING CATEGORY FAILED";

    private final Category category;

    public CreatingCategoryFailedException(String message, Category category) {
        super(message);
        this.category = category;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

    public Category getCategory() {
        return category;
    }

}
