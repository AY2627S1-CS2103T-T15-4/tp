package seedu.address.model.customer.exceptions;

/**
 * Signals that the operation will result in duplicate customers (customers are considered duplicates if they have the
 * same id).
 */
public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException() {
        super("Operation would result in duplicate customers");
    }
}
