package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    default ObservableList<Dog> getDogList() {
        return FXCollections.emptyObservableList();
    }

    /** Returns the next unused positive dog ID, or {@code -1} when unavailable. */
    default int getNextDogId() {
        return -1;
    }

    /** Returns the next unused customer ID, or {@code -1} when unavailable. */
    default int getNextCustomerId() {
        return -1;
    }

    /** Returns an unmodifiable observable collection of activity schedules. */
    default ObservableList<Activity> getActivityList() {
        return FXCollections.emptyObservableList();
    }

    default ObservableList<Enrolment> getEnrolmentList() {
        return FXCollections.emptyObservableList();
    }

    /** Returns the next unused activity ID; older address books start at one. */
    default long getNextActivityId() {
        return 1;
    }

    ObservableList<Customer> getCustomerList();

}
