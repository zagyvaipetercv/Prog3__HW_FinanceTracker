package financetracker.exceptions.purchase;

import financetracker.exceptions.ErrorBoxException;

public class InvalidTableCellException extends ErrorBoxException {
    private static final String ERROR_TITLE = "INVALID CELL VALUE";

    private final int row;
    private final int col;
    private final String textValue;
    private final String columnName;

    public InvalidTableCellException(int row, int col, String textValue, String columnName) {
        this.row = row;
        this.col = col;
        this.textValue = textValue;
        this.columnName = columnName;
    }

    @Override
    public String getErrorTitle() {
        return ERROR_TITLE;
    }
    
    @Override
    public String getMessage() {
        return "Invalid Cell Value at [" + (row + 1) + ";" + (col + 1) + "]" + "(" + columnName + ") " + "value: "  + "\"" + textValue + "\"";
    }
}
