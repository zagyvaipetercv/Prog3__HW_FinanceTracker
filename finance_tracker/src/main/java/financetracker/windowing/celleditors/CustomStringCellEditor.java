package financetracker.windowing.celleditors;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class CustomStringCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField textfield = new JTextField();
    private String currentString;

    @Override
    public Object getCellEditorValue() {
        currentString = textfield.getText();
        return currentString;
    }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
            int column) {
        textfield.setText("");

        return textfield;
    }

}