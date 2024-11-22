package financetracker.views.purchase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.Currency;

import javax.swing.AbstractCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellEditor;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.PurchaseController;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;
import financetracker.exceptions.cashflowcontroller.InvalidAmountException;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.category.CreatingCategoryFailedException;
import financetracker.exceptions.purchase.CreatingPurchaseFailedException;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.exceptions.purchase.InvalidTableCellException;
import financetracker.models.PurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;

public class AddPurchaseView extends FrameView {
    private PurchaseController purchaseController;

    private PurchasedItemTableModel pitm;
    private DatePicker datePicker;

    public AddPurchaseView(PurchaseController purchaseController) {
        this.purchaseController = purchaseController;
        pitm = new PurchasedItemTableModel();

        setTitle("Finance Tracker - Add Purchase");

        initComponents();

        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Date Picker
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Date of Purchase:");
        
        datePicker = new DatePicker();
        datePicker.setDate(LocalDate.now());

        topPanel.add(dateLabel);
        topPanel.add(datePicker);

        add(topPanel, BorderLayout.NORTH);

        // Table
        PurchasedItemsTable purchasedItemsTable = new PurchasedItemsTable(pitm);
        JScrollPane scrollPane = new JScrollPane(purchasedItemsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        FlowLayout buttonPanelLayout = new FlowLayout();
        buttonPanelLayout.setAlignment(FlowLayout.CENTER);
        buttonPanel.setLayout(buttonPanelLayout);

        JButton submitButton = new JButton("Submit");
        JButton deleteRowButton = new JButton("Delete Row");
        JButton cancelButton = new JButton("Cancel");

        deleteRowButton.addActionListener(ae -> {
            try {
                purchaseController.deleteRow(pitm, purchasedItemsTable.getSelectedRow());
            } catch (DeleteUnfinishedEmptyRowException e) {
                ErrorBox.show(this, e);
            }
        });

        cancelButton.addActionListener(ae -> {
            purchaseController.closeFrameView(this);
        });

        submitButton.addActionListener(ae -> {
            try {
                purchaseController.addPurchase(pitm, datePicker.getDate());
            } catch (InvalidTableCellException | CreatingPurchaseFailedException | CategoryLookupFailedException | CreatingCategoryFailedException e) {
                ErrorBox.show(this, e);
            }
        });

        buttonPanel.add(submitButton);
        buttonPanel.add(deleteRowButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private class PurchasedItemsTable extends JTable {
        public PurchasedItemsTable(PurchasedItemTableModel pitm) {
            super(pitm);
            getColumnModel().getColumn(0).setCellEditor(new CategoryCellEditor());
            getColumnModel().getColumn(1).setCellEditor(new CustomStringCellEditor());
            getColumnModel().getColumn(2).setCellEditor(new MoneyCellEditor());
            getColumnModel().getColumn(3).setCellEditor(new CustomDoubleCellEditor());
            getColumnModel().getColumn(4).setCellEditor(new MoneyCellEditor());
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private class CustomDoubleCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textfield = new JTextField();
        private Double currentDouble;

        @Override
        public Object getCellEditorValue() {
            try {
                currentDouble = Double.parseDouble(textfield.getText());
            } catch (Exception e) {
                ErrorBox.show(rootPane, "ERROR", "'" + textfield.getText() + "' is not a valid amount");
                currentDouble = 0.0;
            }

            return currentDouble;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {

            textfield.setText("");

            return textfield;
        }
    }

    private class CustomStringCellEditor extends AbstractCellEditor implements TableCellEditor {
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

    private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textfield = new JTextField();
        private Category currentCategory;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            if (value.getClass().equals(Category.class)) {
                currentCategory = (Category) value;
                textfield.setText("");
            }

            return textfield;
        }

        @Override
        public Object getCellEditorValue() {
            try {
                currentCategory.setName(textfield.getText());
            } catch (Exception e) {
                ErrorBox.show(rootPane, "ERROR", e.getMessage());
            }

            return currentCategory;
        }

    }

    private class MoneyCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textfield = new JTextField();
        private Money currentMoney;

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
                ErrorBox.show(rootPane, e);
            }

            return currentMoney;
        }

    }
}
