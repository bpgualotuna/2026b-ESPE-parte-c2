package es.upm.grise.order;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.grise.order.exceptions.CannotAddItemsToPlacedOrderException;
import es.upm.grise.order.exceptions.CannotPlaceEmptyOrderException;
import es.upm.grise.order.exceptions.IncorrectItemException;
import es.upm.grise.order.exceptions.NonExistingItemException;

public class OrderTest {

    private Order order;
    private Product productA;
    private Product productB;

    @BeforeEach
    public void setUp() {
        order = new Order();
        productA = new Product();
        productA.id = 1L;
        productB = new Product();
        productB.id = 2L;
    }

    // --- Constructor Tests ---

    @Test
    public void testOrderInitialization() {
        assertNotNull(order.getItems(), "Items list should not be null on initialization");
        assertTrue(order.getItems().isEmpty(), "Items list should be empty on initialization");
        assertNull(order.getStatus(), "Status should be null on initialization");
        assertNull(order.getInvoice(), "Invoice should be null on initialization");
    }

    // --- addItem() Tests ---

    @Test
    public void testAddItemToPlacedOrderThrowsException() throws Exception {
        Item item = new Item(productA, 1, 10.0);
        order.addItem(item);
        order.place();
        
        Item newItem = new Item(productB, 2, 15.0);
        assertThrows(CannotAddItemsToPlacedOrderException.class, () -> {
            order.addItem(newItem);
        });
    }

    @Test
    public void testAddItemWithNegativePriceThrowsException() {
        Item item = new Item(productA, 1, -5.0);
        assertThrows(IncorrectItemException.class, () -> {
            order.addItem(item);
        });
    }

    @Test
    public void testAddItemWithZeroQuantityThrowsException() {
        Item item = new Item(productA, 0, 10.0);
        assertThrows(IncorrectItemException.class, () -> {
            order.addItem(item);
        });
    }

    @Test
    public void testAddItemWithNegativeQuantityThrowsException() {
        Item item = new Item(productA, -3, 10.0);
        assertThrows(IncorrectItemException.class, () -> {
            order.addItem(item);
        });
    }

    @Test
    public void testAddItemUpdatesStatusToUnconfirmed() throws Exception {
        Item item = new Item(productA, 1, 10.0);
        order.addItem(item);
        assertEquals(Status.UNCONFIRMED, order.getStatus(), "Status should be UNCONFIRMED after adding the first item");
    }

    @Test
    public void testAddDuplicateItemSamePriceMergesQuantity() throws Exception {
        Item item1 = new Item(productA, 2, 10.0);
        Item item2 = new Item(productA, 3, 10.0);
        order.addItem(item1);
        order.addItem(item2);

        assertEquals(1, order.getItems().size(), "Should merge items with same product ID");
        Item mergedItem = order.getItems().get(0);
        assertEquals(5, mergedItem.getQuantity(), "Merged quantity should be the sum of quantities");
        assertEquals(10.0, mergedItem.getPrice(), 0.001, "Merged price should remain the same");
    }

    @Test
    public void testAddDuplicateItemNewPriceLowerKeepsMaxPrice() throws Exception {
        Item item1 = new Item(productA, 2, 10.0);
        Item item2 = new Item(productA, 3, 5.0);
        order.addItem(item1);
        order.addItem(item2);

        assertEquals(1, order.getItems().size(), "Should merge items with same product ID");
        Item mergedItem = order.getItems().get(0);
        assertEquals(5, mergedItem.getQuantity(), "Merged quantity should be the sum of quantities");
        assertEquals(10.0, mergedItem.getPrice(), 0.001, "Merged price should keep the maximum value");
    }

    @Test
    public void testAddDuplicateItemNewPriceHigherUpdatesToMaxPrice() throws Exception {
        Item item1 = new Item(productA, 2, 5.0);
        Item item2 = new Item(productA, 3, 10.0);
        order.addItem(item1);
        order.addItem(item2);

        assertEquals(1, order.getItems().size(), "Should merge items with same product ID");
        Item mergedItem = order.getItems().get(0);
        assertEquals(5, mergedItem.getQuantity(), "Merged quantity should be the sum of quantities");
        assertEquals(10.0, mergedItem.getPrice(), 0.001, "Merged price should be updated to the higher price");
    }

    // --- removeItem() Tests ---

    @Test
    public void testRemoveExistingItem() throws Exception {
        Item item = new Item(productA, 1, 10.0);
        order.addItem(item);
        order.removeItem(item);
        assertTrue(order.getItems().isEmpty(), "Items list should be empty after removing the only item");
    }

    @Test
    public void testRemoveNonExistingItemThrowsException() {
        Item item = new Item(productA, 1, 10.0);
        assertThrows(NonExistingItemException.class, () -> {
            order.removeItem(item);
        });
    }

    @Test
    public void testRemoveLastItemResetsStatusToNull() throws Exception {
        Item item = new Item(productA, 1, 10.0);
        order.addItem(item);
        assertEquals(Status.UNCONFIRMED, order.getStatus());
        order.removeItem(item);
        assertNull(order.getStatus(), "Status should reset to null when order becomes empty");
    }

    // --- calculateTotalAmount() Tests ---

    @Test
    public void testCalculateTotalAmountEmptyOrder() {
        assertEquals(0.0, order.calculateTotalAmount(), 0.001, "Total amount of empty order should be 0");
    }

    @Test
    public void testCalculateTotalAmountMultipleItems() throws Exception {
        Item item1 = new Item(productA, 2, 10.0); // 20.0
        Item item2 = new Item(productB, 3, 15.0); // 45.0
        order.addItem(item1);
        order.addItem(item2);
        assertEquals(65.0, order.calculateTotalAmount(), 0.001, "Total amount should match sum of price * quantity");
    }

    // --- place() Tests ---

    @Test
    public void testPlaceEmptyOrderThrowsException() {
        assertThrows(CannotPlaceEmptyOrderException.class, () -> {
            order.place();
        });
    }

    @Test
    public void testPlaceOrderCreatesInvoiceAndRegisters() throws Exception {
        Item item = new Item(productA, 1, 10.0);
        order.addItem(item);
        order.place();

        assertEquals(Status.PLACED, order.getStatus(), "Status should be PLACED");
        assertNotNull(order.getInvoice(), "Invoice should be created");
        assertEquals(order, order.getInvoice().getOrder(), "Invoice should reference the order");
        assertTrue(Invoices.getInvoices().contains(order.getInvoice()), "Invoice should be registered in Invoices list");
    }

}
