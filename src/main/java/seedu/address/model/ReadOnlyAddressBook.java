package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    default ObservableList<Dog> getDogList() {
        return FXCollections.emptyObservableList();
    }

    /** Returns the next unused dog ID, or {@code -1} when unavailable. */
    default int getNextDogId() {
        return -1;
    }

    /** Returns the next unused customer ID, or {@code -1} when unavailable. */
    default int getNextCustomerId() {
        return -1;
    }

    ObservableList<Customer> getCustomerList();

}
