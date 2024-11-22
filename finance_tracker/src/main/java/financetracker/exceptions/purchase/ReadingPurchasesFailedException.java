package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class ReadingPurchasesFailedException extends ErrorBoxException {

    private static final String ERROR_TITLE = "FAIELD READING PURCHASES";

    public ReadingPurchasesFailedException(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
