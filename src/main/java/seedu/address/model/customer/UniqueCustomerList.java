//@@author its-shoutime
package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.customer.exceptions.CustomerNotFoundException;
import seedu.address.model.customer.exceptions.DuplicateCustomerException;

/**
 * A list of customers that enforces uniqueness between its elements and does not allow nulls.
 * A customer is considered unique by comparing using {@code Customer#isSameCustomer(Customer)}.
 *
 * @see Customer#isSameCustomer(Customer)
 */
public class UniqueCustomerList implements Iterable<Customer> {

    private static final Logger logger = LogsCenter.getLogger(UniqueCustomerList.class);

    private final ObservableList<Customer> internalList = FXCollections.observableArrayList();
    private final ObservableList<Customer> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains a customer with the same id as {@code toCheck}.
     */
    public boolean contains(Customer toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSameCustomer);
    }

    /**
     * Adds a customer to the list.
     * The customer must not already exist in the list.
     */
    public void add(Customer toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            logger.warning("Rejected a duplicate customer ID " + toAdd.getId());
            throw new DuplicateCustomerException();
        }
        internalList.add(toAdd);
    }

    // @@author lilosy
    /**
     * Replaces the customer {@code target} in the list with {@code editedCustomer}.
     * {@code target} must exist in the list.
     * The customer identity of {@code editedCustomer} must not be the same as another existing customer in the list.
     */
    public void setCustomer(Customer target, Customer editedCustomer) {
        requireAllNonNull(target, editedCustomer);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new CustomerNotFoundException();
        }

        if (!target.isSameCustomer(editedCustomer) && contains(editedCustomer)) {
            throw new DuplicateCustomerException();
        }

        internalList.set(index, editedCustomer);
    }
    // @@author its-shoutime

    /**
     * Removes the equivalent customer from the list.
     * The customer must exist in the list.
     */
    public void remove(Customer toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new CustomerNotFoundException();
        }
    }

    /**
     * Replaces the contents of this list with {@code customers}.
     * {@code customers} must not contain duplicate customers.
     */
    public void setCustomers(List<Customer> customers) {
        requireAllNonNull(customers);
        if (!customersAreUnique(customers)) {
            logger.warning("Rejected a customer list containing duplicate IDs.");
            throw new DuplicateCustomerException();
        }

        internalList.setAll(customers);
    }

    // @@author lilosy
    /** Replaces the contents of this list with those from {@code replacement}. */
    public void setCustomers(UniqueCustomerList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }
    // @@author its-shoutime

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Customer> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Customer> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof UniqueCustomerList otherUniqueCustomerList)) {
            return false;
        }

        return internalList.equals(otherUniqueCustomerList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code customers} contains only unique customers.
     */
    private boolean customersAreUnique(List<Customer> customers) {
        for (int i = 0; i < customers.size() - 1; i++) {
            for (int j = i + 1; j < customers.size(); j++) {
                if (customers.get(i).isSameCustomer(customers.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
