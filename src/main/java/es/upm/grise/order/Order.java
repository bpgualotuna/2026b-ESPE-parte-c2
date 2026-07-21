package es.upm.grise.order;

import java.util.ArrayList;

import es.upm.grise.order.exceptions.CannotAddItemsToPlacedOrderException;
import es.upm.grise.order.exceptions.CannotPlaceEmptyOrderException;
import es.upm.grise.order.exceptions.IncorrectItemException;
import es.upm.grise.order.exceptions.NonExistingItemException;

public class Order {

    private ArrayList<Item> items;
    private Status status;
    private Invoice invoice;

    /*
     * Method to code/test
     */
    public Order() {
        this.items = new ArrayList<Item>();
        this.status = null;
        this.invoice = null;
    }

    /*
     * Method to code/test
     */
    public void addItem(Item item) throws CannotAddItemsToPlacedOrderException, IncorrectItemException {
        if (status == Status.PLACED) {
            throw new CannotAddItemsToPlacedOrderException();
        }
        if (item.getPrice() < 0 || item.getQuantity() <= 0) {
            throw new IncorrectItemException();
        }

        boolean found = false;
        for (Item existingItem : items) {
            if (existingItem.getProduct().getId() == item.getProduct().getId()) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                if (existingItem.getPrice() != item.getPrice()) {
                    existingItem.setPrice(Math.max(existingItem.getPrice(), item.getPrice()));
                }
                found = true;
                break;
            }
        }

        if (!found) {
            items.add(item);
        }

        if (status == null && !items.isEmpty()) {
            status = Status.UNCONFIRMED;
        }
    }

    /*
     * Method to code/test
     */
    public void removeItem(Item item) throws NonExistingItemException {
        if (!items.remove(item)) {
            throw new NonExistingItemException();
        }
        if (items.isEmpty()) {
            status = null;
        }
    }

    public double calculateTotalAmount() {
        double total = 0;
        for (Item item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public void place() throws CannotPlaceEmptyOrderException {
        if (items == null || items.isEmpty()) {
            throw new CannotPlaceEmptyOrderException();
        }
        this.status = Status.PLACED;
        this.invoice = new Invoice();
        this.invoice.setOrder(this);
        Invoices.add(this.invoice);
    }

    /*
     * getters
     */

    public ArrayList<Item> getItems() {
        return items;
    }

    public Status getStatus() {
        return status;
    }

    public Invoice getInvoice() {
        return invoice;
    }

}