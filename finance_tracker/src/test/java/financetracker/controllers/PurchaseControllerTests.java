package financetracker.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import financetracker.datatypes.CashFlow;
import financetracker.datatypes.Category;
import financetracker.datatypes.Money;
import financetracker.datatypes.Purchase;
import financetracker.datatypes.PurchasedItem;
import financetracker.exceptions.purchase.DeleteUnfinishedEmptyRowException;
import financetracker.models.PurchasedItemTableModel;

class PurchaseControllerTests extends ControllerTests {
    PurchaseController purchaseController;
    CashFlowController cashFlowController;

    Category foodCategory;
    Category hygienaCategory;
    Category otherCategory;

    PurchasedItem bread;
    PurchasedItem toothPaste;
    PurchasedItem gift;
    PurchasedItem tomato;

    LocalDate dateOfPurchase;

    @BeforeEach
    void setup() {
        purchaseController = mainFrame.getPurchaseController();
        cashFlowController = mainFrame.getCashFlowController();

        foodCategory = new Category(-1, "FOOD");
        hygienaCategory = new Category(-1, "HYGIENE");
        otherCategory = new Category(-1, "OTHERS");

        bread = new PurchasedItem(null, foodCategory, "bread",
                new Money(699, Currency.getInstance("HUF")), 2);

        toothPaste = new PurchasedItem(null, hygienaCategory, "tooth paste",
                new Money(1299, Currency.getInstance("HUF")), 1);

        gift = new PurchasedItem(null, otherCategory, "gift",
                new Money(2999, Currency.getInstance("HUF")), 1);

        tomato = new PurchasedItem(null, foodCategory, "tomato", new Money(599, Currency.getInstance("HUF")), 1.5);

        dateOfPurchase = LocalDate.of(2000, 01, 01);
    }

    @Test
    void testAddPurchase() throws Exception {
        // PRE
        List<PurchasedItem> items = new ArrayList<>();
        items.add(bread);
        items.add(toothPaste);
        items.add(gift);

        List<PurchasedItem> items2 = new ArrayList<>();
        items2.add(tomato);

        // ACT
        PurchasedItemTableModel pitm = new PurchasedItemTableModel(items);
        purchaseController.addPurchase(pitm, dateOfPurchase);

        pitm = new PurchasedItemTableModel(items2);
        purchaseController.addPurchase(pitm, dateOfPurchase);

        // ASSERTS
        List<Purchase> purchases = purchaseController.getAllPurchases();
        assertEquals(2, purchases.size());

        // Purchase 1
        Purchase purchase1 = purchases.get(0);
        assertEquals(1, purchase1.getId());
        assertEquals(LocalDate.of(2000, 01, 01), purchase1.getDateOfPurchase());
        assertEquals(new Money(5696, Currency.getInstance("HUF")), purchase1.getSumPrice()); // 5696 = 2 * 699 + 1299 +
                                                                                             // 2999

        List<PurchasedItem> purchasedItems = purchase1.getBoughtItems();
        assertEquals(3, purchasedItems.size());

        PurchasedItem purchasedItem1_1 = purchasedItems.get(0);

        assertEquals(1, purchasedItem1_1.getCategory().getId());
        assertEquals("FOOD", purchasedItem1_1.getCategory().getName());

        assertEquals("bread", purchasedItem1_1.getName());

        assertEquals(new Money(699, Currency.getInstance("HUF")), purchasedItem1_1.getPricePerUnit());
        assertEquals(new Money(1398, Currency.getInstance("HUF")), purchasedItem1_1.getSumPrice()); // 1398 = 2 * 699

        assertEquals(2, purchasedItem1_1.getAmount());

        // Purchase 2
        Purchase purchase2 = purchases.get(1);
        assertEquals(1, purchase2.getBoughtItems().size());

        PurchasedItem purchasedItem2_1 = purchase2.getBoughtItems().get(0);
        assertEquals(1, purchasedItem2_1.getCategory().getId());
        assertEquals("FOOD", purchasedItem2_1.getCategory().getName());

        assertEquals("tomato", purchasedItem2_1.getName());

        // CashFlows
        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();
        assertEquals(2, cashFlows.size()); // Created 2 cashflows
        assertEquals(-5696, cashFlows.get(0).getMoney().getAmount());
        assertEquals(-898.5, cashFlows.get(1).getMoney().getAmount());
    }

    @Test
    void testEditPurchasedItem() throws Exception {
        // ADDING A PURCHASE
        List<PurchasedItem> items = new ArrayList<>();
        items.add(bread);

        PurchasedItemTableModel pitm = new PurchasedItemTableModel(items);
        purchaseController.addPurchase(pitm, dateOfPurchase);
        assertEquals(1, purchaseController.getAllPurchases().size()); // Only has one record

        Purchase purchase = purchaseController.getAllPurchases().get(0);
        assertEquals(1, purchase.getId()); // That record has the id of 1

        assertEquals(1, purchase.getBoughtItems().size());
        assertEquals("bread", purchase.getBoughtItems().get(0).getName()); // The only item in the purchase is a bread

        // EDITING THE PURCHASE
        items = new ArrayList<>();
        items.add(toothPaste);
        items.add(gift);
        pitm = new PurchasedItemTableModel(items);

        purchaseController.editPurchase(purchase, pitm, LocalDate.of(2001, 02, 02));

        assertEquals(1, purchaseController.getAllPurchases().size()); // Still only has 1 purchase

        purchase = purchaseController.getAllPurchases().get(0);
        assertEquals(1, purchase.getId()); // That purchase is still the same purchase with the id of 1

        assertEquals(2, purchase.getBoughtItems().size()); // Edited purchase now has 2 items
        assertEquals(LocalDate.of(2001, 02, 02), purchase.getDateOfPurchase());
        assertEquals("tooth paste", purchase.getBoughtItems().get(0).getName()); // One of them is now a tooth paste
        assertEquals("gift", purchase.getBoughtItems().get(1).getName()); // The other is a gift

        // Changed cashflow
        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();
        assertEquals(1, cashFlows.size());
        assertEquals(1, cashFlows.get(0).getId());
        assertEquals(new Money(-4298, Currency.getInstance("HUF")), cashFlows.get(0).getMoney());

    }

    @Test
    void deletingRowInTableModel() throws Exception {
        List<PurchasedItem> purchasedItems = new ArrayList<>();
        purchasedItems.add(bread);
        purchasedItems.add(toothPaste);
        purchasedItems.add(gift);

        PurchasedItemTableModel pitm = new PurchasedItemTableModel(purchasedItems);

        purchaseController.deleteRow(pitm, 1);

        List<PurchasedItem> result = pitm.getItems();

        assertEquals(2, result.size());
        assertEquals("bread", result.get(0).getName());
        assertEquals("gift", result.get(1).getName());
    }

    @Test
    void deletingUnfinishedRowInTableModel() {
        List<PurchasedItem> purchasedItems = new ArrayList<>();
        purchasedItems.add(bread);

        PurchasedItemTableModel pitm = new PurchasedItemTableModel(purchasedItems);

        assertThrows(
                DeleteUnfinishedEmptyRowException.class,
                () -> purchaseController.deleteRow(pitm, 1));
    }

    @Test
    void testDeletingPurchase() throws Exception {
        // PRE
        // Adding first set of purchase
        List<PurchasedItem> items = new ArrayList<>();
        items.add(bread);
        items.add(toothPaste);

        PurchasedItemTableModel pitm = new PurchasedItemTableModel(items);
        purchaseController.addPurchase(pitm, dateOfPurchase);

        // Adding second set of purchase
        items = new ArrayList<>();
        items.add(gift);

        pitm = new PurchasedItemTableModel(items);

        purchaseController.addPurchase(pitm, dateOfPurchase);

        // ACT
        Purchase purchase1 = purchaseController.getAllPurchases().get(0);
        purchaseController.deletePurchase(purchase1);

        // ASSERTS
        assertEquals(1, purchaseController.getAllPurchases().size()); // Now has only one purchase
        assertEquals("gift", purchaseController.getAllPurchases().get(0).getBoughtItems().get(0).getName()); // The purchase with the gift

        List<CashFlow> cashFlows = cashFlowController.getAllCashFlows();
        assertEquals(1, cashFlows.size());
        assertEquals(2, cashFlows.get(0).getId());
        assertEquals(new Money(-2999, Currency.getInstance("HUF")), cashFlows.get(0).getMoney());
    }
}
