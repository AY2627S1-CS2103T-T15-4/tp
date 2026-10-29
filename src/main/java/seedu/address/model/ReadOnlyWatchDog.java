package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * Unmodifiable view of a WatchDog data file
 */
public interface ReadOnlyWatchDog {

    default ObservableList<Dog> getDogList() {
        return FXCollections.emptyObservableList();
    }

    /**
     * Returns the next unused positive dog ID, {@code Integer.MAX_VALUE + 1} when IDs are exhausted,
     * or {@code -1} when the counter is unavailable in legacy data.
     */
    default long getNextDogId() {
        return -1;
    }

    /** Returns the next unused customer ID, MAX_VALUE + 1 when exhausted, or {@code -1} when unavailable. */
    default long getNextCustomerId() {
        return -1;
    }

    /** Returns an unmodifiable observable collection of activity schedules. */
    default ObservableList<Activity> getActivityList() {
        return FXCollections.emptyObservableList();
    }

    /**
     * Returns a live unmodifiable observable collection of dated enrolments.
     *
     * @return the dated enrolments
     */
    default ObservableList<Enrolment> getEnrolmentList() {
        return FXCollections.emptyObservableList();
    }

    /** Returns the next unused activity ID; older WatchDog files start at one. */
    default long getNextActivityId() {
        return 1;
    }

    ObservableList<Customer> getCustomerList();

}
