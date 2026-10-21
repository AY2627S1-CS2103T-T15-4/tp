package seedu.address.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.exceptions.EnrolException;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;

/**
 * The API of the Model component.
 */
public interface Model {
    Predicate<Customer> PREDICATE_SHOW_ALL_CUSTOMERS = unused -> true;

    Predicate<Activity> PREDICATE_SHOW_ALL_ACTIVITIES = unused -> true;

    void enrolDogs(int activityId, List<Integer> dogIds) throws EnrolException;

    void unenrolDogs(int activityId, List<Integer> dogIds) throws EnrolException;

    void markActivity(int activityId);

    void unmarkActivity(int activityId);

    /** Links an entire batch or throws without changing records or filters. */
    void linkDogs(int customerId, List<Integer> dogIds) throws LinkException;

    /** Unlinks an entire batch or throws without changing records or filters. Records are never deleted. */
    void unlinkDogs(List<Integer> dogIds) throws UnlinkException;

    /** Unlinks all owned dogs; throws if the customer is missing or has no dogs. Preserves records and filters. */
    void unlinkCustomerDogs(int customerId) throws UnlinkException;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    boolean hasCustomer(Customer customer);

    /**
     * Deletes the given customer and unlinks their dogs, preserving the dog records.
     * The customer must exist in the address book.
     */
    void deleteCustomer(Customer customer);

    /**
     * Adds the given customer.
     * {@code customer} must not already exist in the address book.
     */
    void addCustomer(Customer customer);

    /**
     * Replaces the given customer {@code target} with {@code editedCustomer}.
     * {@code target} must exist in the address book.
     * {@code editedCustomer} must preserve the target's persistent ID so that dog ownership stays valid.
     */
    void setCustomer(Customer target, Customer editedCustomer);

    default ObservableList<Dog> getDogList() {
        return getAddressBook().getDogList();
    }

    /** Returns an unmodifiable observable view of filtered dogs. */
    ObservableList<Dog> getFilteredDogList();

    /** Updates the dog filter; the predicate must not be null. */
    void updateFilteredDogList(Predicate<Dog> predicate);

    ObservableList<Customer> getFilteredCustomerList();

    /** Returns an unmodifiable view of every stored customer. */
    default ObservableList<Customer> getCustomerList() {
        return getAddressBook().getCustomerList();
    }

    void updateFilteredCustomerList(Predicate<Customer> predicate);

    /** Adds an empty schedule and returns it with its persistent ID. */
    Activity addActivity(ActivityName name, LocalDate date, LocalTime start, LocalTime end, Recurrence recurrence);

    /** Returns all schedules, independently of dog/customer filters. */
    default ObservableList<Activity> getActivityList() {
        return getAddressBook().getActivityList();
    }

    /** Returns an unmodifiable observable view of filtered activities. */
    ObservableList<Activity> getFilteredActivityList();

    /** Updates the activity filter; the predicate must not be null. */
    void updateFilteredActivityList(Predicate<Activity> predicate);

    /**
     * Shows this dog, its current owner, and its current incomplete activities.
     * Later changes to those records update the same three lists.
     */
    void selectDog(int dogId);

    /** Adds a dog and returns the dog with its assigned persistent ID. */
    Dog addDog(Dog dog);

    /** Returns whether a dog with the same persistent ID exists. */
    boolean hasDog(Dog dog);

    /** Deletes the given dog. The dog must exist in the address book. */
    void deleteDog(Dog dog);

    /** Replaces an existing dog with its edited version. */
    void setDog(Dog target, Dog editedDog);
}
