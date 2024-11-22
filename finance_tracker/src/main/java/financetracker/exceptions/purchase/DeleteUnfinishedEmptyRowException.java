package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class DeleteUnfinishedEmptyRowException extends ErrorBoxException {
    private static final String ERROR_TITLE = "ROW DELETE ERROR";

    public DeleteUnfinishedEmptyRowException(String message) {
        super(message);
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }

}
