package financetracker.windowing.celleditors;

import java.awt.Component;
import java.util.Currency;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.windowing.ErrorBox;

public class MoneyCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField textfield = new JTextField();
    private Money currentMoney;

    private final Component parentComponent;

    public MoneyCellEditor(Component parentComponent) {
        this.parentComponent = parentComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
            int column) {
        if (value.getClass().equals(Money.class)) {
            currentMoney = (Money) value;
            textfield.setText("");
        }

        return textfield;
    }

    @Override
    public Object getCellEditorValue() {
        try {
            String[] parts = textfield.getText().split(" ");
            double amount = Money.parseAmount(parts[0]);
            Currency currency = (parts.length == 2 ? Money.parseCurrency(parts[1]) : Currency.getInstance("HUF"));
            currentMoney.setAmount(amount);
            currentMoney.setCurrency(currency);
        } catch (InvalidAmountException e) {
            ErrorBox.show(parentComponent, e);
        }

        return currentMoney;
    }

}