package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class UpadtingPurchaseModelFailed extends ErrorBoxException {

    private static final String ERROR_TITLE = "UPDATING MODEL FAILED";

    public UpadtingPurchaseModelFailed(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
