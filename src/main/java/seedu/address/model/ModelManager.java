package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.exceptions.EnrolException;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Customer> filteredCustomers;
    private final FilteredList<Dog> filteredDogs;
    private final FilteredList<Activity> filteredActivities;
    private Predicate<Customer> activeCustomerPredicate;
    private Predicate<Dog> activeDogPredicate;
    private Integer selectedDogId;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredCustomers = new FilteredList<>(this.addressBook.getCustomerList());
        filteredDogs = new FilteredList<>(this.addressBook.getDogList());
        filteredActivities = new FilteredList<>(this.addressBook.getActivityList());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
        refreshSelectedDog();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public Activity addActivity(ActivityName name, LocalDate date, LocalTime start, LocalTime end,
            Recurrence recurrence) {
        Activity activity = addressBook.addActivity(name, date, start, end, recurrence);
        refreshSelectedDog();
        return activity;
    }

    //=========== Dog Operations ============================================================================
    @Override
    public void enrolDogs(int activityId, List<Integer> dogIds) throws EnrolException {
        addressBook.enrolDogs(activityId, dogIds);
        refreshSelectedDog();
    }

    @Override
    public void unenrolDogs(int activityId, List<Integer> dogIds) throws EnrolException {
        addressBook.unenrolDogs(activityId, dogIds);
        refreshSelectedDog();
    }

    @Override
    public void markActivity(int activityId) {
        addressBook.markActivity(activityId);
        refreshSelectedDog();
    }

    @Override
    public void unmarkActivity(int activityId) {
        addressBook.unmarkActivity(activityId);
        refreshSelectedDog();
    }

    @Override
    public void linkDogs(int customerId, List<Integer> dogIds) throws LinkException {
        addressBook.linkDogs(customerId, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unlinkDogs(List<Integer> dogIds) throws UnlinkException {
        addressBook.unlinkDogs(dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unlinkCustomerDogs(int customerId) throws UnlinkException {
        addressBook.unlinkCustomerDogs(customerId);
        refreshDisplayedRecords();
    }

    @Override
    public boolean hasDog(Dog dog) {
        return addressBook.hasDog(dog);
    }

    @Override
    public void deleteDog(Dog dog) {
        addressBook.removeDog(dog);
        refreshDisplayedRecords();
    }

    @Override
    public void setDog(Dog target, Dog editedDog) {
        addressBook.setDog(target, editedDog);
        refreshDisplayedRecords();
    }

    @Override
    public Dog addDog(Dog dog) {
        Dog addedDog = addressBook.addDog(dog);
        refreshDisplayedRecords();
        return addedDog;
    }

    //===========Customer related methods==================================
    @Override
    public boolean hasCustomer(Customer customer) {
        requireNonNull(customer);
        return addressBook.hasCustomer(customer);
    }

    @Override
    public void deleteCustomer(Customer target) {
        addressBook.removeCustomer(target);
        refreshDisplayedRecords();
    }

    @Override
    public void addCustomer(Customer customer) {
        addressBook.addCustomer(customer);
        refreshDisplayedRecords();
    }

    @Override
    public void setCustomer(Customer target, Customer editedCustomer) {
        requireAllNonNull(target, editedCustomer);

        addressBook.setCustomer(target, editedCustomer);
        refreshDisplayedRecords();
    }

    //=========== Filtered Record List Accessors =============================================================

    @Override
    public ObservableList<Dog> getDogList() {
        return addressBook.getDogList();
    }

    @Override
    public ObservableList<Dog> getFilteredDogList() {
        return filteredDogs;
    }

    @Override
    public void updateFilteredDogList(Predicate<Dog> predicate) {
        requireNonNull(predicate);
        clearDogSelection();
        activeDogPredicate = predicate;
        filteredDogs.setPredicate(predicate);
    }

    @Override
    public ObservableList<Customer> getFilteredCustomerList() {
        return filteredCustomers;
    }

    @Override
    public void updateFilteredCustomerList(Predicate<Customer> predicate) {
        requireNonNull(predicate);
        clearDogSelection();
        activeCustomerPredicate = predicate;
        filteredCustomers.setPredicate(predicate);
    }

    @Override
    public ObservableList<Activity> getFilteredActivityList() {
        return filteredActivities;
    }

    @Override
    public void updateFilteredActivityList(Predicate<Activity> predicate) {
        requireNonNull(predicate);
        clearDogSelection();
        filteredActivities.setPredicate(predicate);
    }

    @Override
    public void selectDog(int dogId) {
        selectedDogId = dogId;
        applySelectedDog();
    }

    private void clearDogSelection() {
        selectedDogId = null;
    }

    /** Reapplies the current dog selection after stored records change. */
    private void refreshSelectedDog() {
        if (selectedDogId != null) {
            applySelectedDog();
        }
    }

    /** Reapplies active filters after stored records or relationships change. */
    private void refreshDisplayedRecords() {
        if (selectedDogId != null) {
            refreshSelectedDog();
            return;
        }
        if (activeCustomerPredicate != null) {
            filteredCustomers.setPredicate(null);
            filteredCustomers.setPredicate(activeCustomerPredicate);
        }
        if (activeDogPredicate != null) {
            filteredDogs.setPredicate(null);
            filteredDogs.setPredicate(activeDogPredicate);
        }
    }

    private void applySelectedDog() {
        int dogId = selectedDogId;
        filteredDogs.setPredicate(dog -> dog.getId() == dogId);
        filteredCustomers.setPredicate(customer -> ownerIdOf(dogId)
                .filter(ownerId -> ownerId == customer.getId()).isPresent());
        filteredActivities.setPredicate(activity -> isVisibleForSelectedDog(dogId, activity));
    }

    private Optional<Integer> ownerIdOf(int dogId) {
        return addressBook.getDogList().stream()
                .filter(dog -> dog.getId() == dogId)
                .findFirst()
                .flatMap(Dog::getOwnerId);
    }

    /**
     * Enrolment currently attaches a dog to a whole schedule, not to one occurrence date.
     * A recurring schedule therefore stays visible while it still occurs today or later.
     * A past one-time schedule is hidden. When enrolment stores an occurrence date, this check
     * should keep only enrolled dates that are today or later.
     */
    private boolean isVisibleForSelectedDog(int dogId, Activity activity) {
        boolean enrolled = addressBook.getEnrolmentList().stream().anyMatch(enrolment ->
                enrolment.getDogId() == dogId && enrolment.getActivityId() == activity.getId());
        return enrolled && !activity.isCompleted()
                && activity.nextOccurrenceOnOrAfter(LocalDate.now()).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredCustomers.equals(otherModelManager.filteredCustomers)
                && filteredDogs.equals(otherModelManager.filteredDogs)
                && filteredActivities.equals(otherModelManager.filteredActivities)
                && Objects.equals(selectedDogId, otherModelManager.selectedDogId);
    }

}
