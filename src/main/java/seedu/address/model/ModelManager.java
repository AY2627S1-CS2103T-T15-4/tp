package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
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
    private Predicate<Activity> activeActivityPredicate;
    private Integer selectedDogId;
    private Integer selectedCustomerId;
    private Predicate<Dog> enrolmentDogPredicate;

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

    @Override
    public ModelState createState() {
        return new ModelState(new AddressBook(addressBook), activeCustomerPredicate, activeDogPredicate,
                activeActivityPredicate, selectedDogId, selectedCustomerId, enrolmentDogPredicate);
    }

    @Override
    public void restoreState(ModelState state) {
        requireNonNull(state);
        addressBook.resetData(state.addressBook());
        activeCustomerPredicate = state.customerPredicate();
        activeDogPredicate = state.dogPredicate();
        activeActivityPredicate = state.activityPredicate();
        selectedDogId = state.selectedDogId();
        selectedCustomerId = state.selectedCustomerId();
        enrolmentDogPredicate = state.enrolmentDogPredicate();
        filteredCustomers.setPredicate(null);
        filteredDogs.setPredicate(null);
        filteredActivities.setPredicate(null);
        refreshDisplayedRecords();
        // Some restored predicates read another filtered list; re-evaluate after their source is installed.
        refreshDisplayedRecords();
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
        refreshDisplayedRecords();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public Activity addActivity(ActivityName name, LocalDate date, LocalTime start, LocalTime end,
            Recurrence recurrence) {
        Activity activity = addressBook.addActivity(name, date, start, end, recurrence);
        refreshDisplayedRecords();
        return activity;
    }

    @Override
    public int setActivity(Activity target, Activity editedActivity) {
        return addressBook.setActivity(target, editedActivity);
    }

    @Override
    public void deleteActivity(Activity target) {
        addressBook.removeActivity(target);
    }

    @Override
    public void showActivitiesOnOrAfter(LocalDate date) {
        requireNonNull(date);
        updateFilteredActivityList(activity -> activity.nextOccurrenceOnOrAfter(date).isPresent());
    }

    //=========== Dog Operations ============================================================================
    @Override
    public void enrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        addressBook.enrolDogs(activityId, date, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unenrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        addressBook.unenrolDogs(activityId, date, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public List<Dog> getDogsEnrolledOn(LocalDate date) {
        requireNonNull(date);
        Set<Integer> enrolledDogIds = addressBook.getEnrolmentList().stream()
                .filter(enrolment -> enrolment.getDate().equals(date))
                .map(enrolment -> enrolment.getDogId())
                .collect(Collectors.toSet());
        return addressBook.getDogList().stream()
                .filter(dog -> enrolledDogIds.contains(dog.getId()))
                .toList();
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
        clearSelection();
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
        clearSelection();
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
        clearSelection();
        activeActivityPredicate = predicate;
        filteredActivities.setPredicate(predicate);
        enrolmentDogPredicate = null;
    }

    @Override
    public void setActivityEnrolmentDogPredicate(Predicate<Dog> predicate) {
        enrolmentDogPredicate = predicate;
    }

    @Override
    public String getActivityEnrolmentSummary(Activity activity) {
        requireNonNull(activity);
        if (enrolmentDogPredicate == null) {
            return "";
        }
        List<Dog> matchingDogs = addressBook.getDogList().stream()
                .filter(enrolmentDogPredicate)
                .sorted(java.util.Comparator.comparingInt(Dog::getId))
                .toList();
        StringJoiner summary = new StringJoiner("\n", "Enrolments:\n", "");
        matchingDogs.forEach(dog -> {
            List<LocalDate> dates = addressBook.getEnrolmentList().stream()
                    .filter(enrolment -> enrolment.getActivityId() == activity.getId()
                            && enrolment.getDogId() == dog.getId())
                    .map(Enrolment::getDate)
                    .distinct()
                    .sorted()
                    .toList();
            if (!dates.isEmpty()) {
                summary.add(dog.getId() + ". " + dog.getName().fullName + ": "
                        + dates.stream().map(LocalDate::toString).collect(Collectors.joining(", ")));
            }
        });
        return summary.toString().equals("Enrolments:\n") ? "" : summary.toString();
    }

    @Override
    public void selectDog(int dogId) {
        assert dogId >= 1 : "Dog ID must be positive";
        selectedCustomerId = null;
        selectedDogId = dogId;
        applySelectedDog();
    }

    @Override
    public void selectCustomer(int customerId) {
        assert customerId >= 1 : "Customer ID must be positive";
        selectedDogId = null;
        selectedCustomerId = customerId;
        applySelectedCustomer();
    }

    private void clearSelection() {
        selectedDogId = null;
        selectedCustomerId = null;
    }

    /** Reapplies the current dog selection after stored records change. */
    private void refreshSelectedDog() {
        applySelectedDog();
    }

    /** Reapplies active filters after stored records or relationships change. */
    private void refreshDisplayedRecords() {
        if (selectedDogId != null) {
            refreshSelectedDog();
            return;
        }
        if (selectedCustomerId != null) {
            applySelectedCustomer();
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
        if (activeActivityPredicate != null) {
            filteredActivities.setPredicate(null);
            filteredActivities.setPredicate(activeActivityPredicate);
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
        for (Dog dog : addressBook.getDogList()) {
            if (dog.getId() == dogId) {
                return dog.getOwnerId();
            }
        }
        return Optional.empty();
    }

    /** Shows an activity for a selected dog when it has a dated enrolment today or later. */
    private boolean isVisibleForSelectedDog(int dogId, Activity activity) {
        requireNonNull(activity);
        LocalDate today = LocalDate.now();
        return addressBook.getEnrolmentList().stream().anyMatch(enrolment ->
                enrolment.getDogId() == dogId && enrolment.getActivityId() == activity.getId()
                        && !enrolment.getDate().isBefore(today) && activity.occursOn(enrolment.getDate()));
    }

    private void applySelectedCustomer() {
        int customerId = selectedCustomerId;
        filteredCustomers.setPredicate(customer -> customer.getId() == customerId);
        filteredDogs.setPredicate(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customerId).isPresent());
        filteredActivities.setPredicate(activity -> isVisibleForSelectedCustomer(customerId, activity));
    }

    private boolean isVisibleForSelectedCustomer(int customerId, Activity activity) {
        return addressBook.getDogList().stream()
                .filter(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customerId).isPresent())
                .anyMatch(dog -> isVisibleForSelectedDog(dog.getId(), activity));
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
                && Objects.equals(selectedDogId, otherModelManager.selectedDogId)
                && Objects.equals(selectedCustomerId, otherModelManager.selectedCustomerId);
    }

}
