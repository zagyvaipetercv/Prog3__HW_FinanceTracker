package financetracker.views.purchase;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.github.lgooddatepicker.components.DatePicker;

import financetracker.controllers.PurchaseController;
import financetracker.exceptions.category.CategoryLookupFailedException;
import financetracker.exceptions.generic.CreatingRecordFailed;
import financetracker.exceptions.generic.UpdatingModelFailed;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.exceptions.purchase.InvalidTableCellException;
import financetracker.models.PurchasedItemTableModel;
import financetracker.views.base.FrameView;
import financetracker.windowing.ErrorBox;
import financetracker.windowing.customtables.PurchasedItemsTable;

public class AddPurchaseView extends FrameView {
    private PurchaseController purchaseController;

    private PurchasedItemTableModel pitm;
    private DatePicker datePicker;

    private PurchasedItemsTable purchasedItemsTable;

    private JButton submitButton;
    private JButton deleteRowButton;
    private JButton cancelButton;

    public AddPurchaseView(PurchaseController purchaseController) {
        this.purchaseController = purchaseController;
        pitm = new PurchasedItemTableModel();

        setTitle("Add Purchase");

        initComponents();
        addButtonListeners();

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
        purchasedItemsTable = new PurchasedItemsTable(pitm);
        JScrollPane scrollPane = new JScrollPane(purchasedItemsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        FlowLayout buttonPanelLayout = new FlowLayout();
        buttonPanelLayout.setAlignment(FlowLayout.CENTER);
        buttonPanel.setLayout(buttonPanelLayout);

        submitButton = new JButton("Submit");
        deleteRowButton = new JButton("Delete Row");
        cancelButton = new JButton("Cancel");;

        buttonPanel.add(submitButton);
        buttonPanel.add(deleteRowButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addButtonListeners() {
        deleteRowButton.addActionListener(ae -> {
            try {
                purchaseController.deleteRow(pitm, purchasedItemsTable.getSelectedRow());
            } catch (DeleteUnfinishedEmptyRowException e) {
                ErrorBox.show(this, e);
            }
        });

        cancelButton.addActionListener(ae -> purchaseController.closeFrameView(this));

        submitButton.addActionListener(ae -> {
                try {
                    purchaseController.addPurchase(pitm, datePicker.getDate());
                    purchaseController.refreshPurchaseView();
                    purchaseController.closeFrameView(this);
                } catch (InvalidTableCellException | CreatingRecordFailed | UpdatingModelFailed e) {
                    ErrorBox.show(this, e);
                }
        });
    }
}
