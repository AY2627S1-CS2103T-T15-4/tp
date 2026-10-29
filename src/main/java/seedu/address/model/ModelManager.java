package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
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
 * Represents the in-memory model of WatchDog data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final WatchDog watchDog;
    private final UserPrefs userPrefs;
    private final FilteredList<Customer> filteredCustomers;
    private final FilteredList<Dog> filteredDogs;
    private final FilteredList<Activity> filteredActivities;
    private Predicate<Customer> activeCustomerPredicate;
    private Predicate<Dog> activeDogPredicate;
    private Predicate<Activity> activeActivityPredicate;
    private Predicate<Dog> enrolmentDogPredicate;
    private final SimpleIntegerProperty enrolmentContextVersion = new SimpleIntegerProperty();

    /**
     * Initializes a ModelManager with the given watchDog and userPrefs.
     */
    public ModelManager(ReadOnlyWatchDog watchDog, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(watchDog, userPrefs);

        logger.fine("Initializing with WatchDog data: " + watchDog + " and user prefs " + userPrefs);

        this.watchDog = new WatchDog(watchDog);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredCustomers = new FilteredList<>(this.watchDog.getCustomerList());
        filteredDogs = new FilteredList<>(this.watchDog.getDogList());
        filteredActivities = new FilteredList<>(this.watchDog.getActivityList());
    }

    public ModelManager() {
        this(new WatchDog(), new UserPrefs());
    }

    @Override
    public ModelState createState() {
        return new ModelState(new WatchDog(watchDog), activeCustomerPredicate, activeDogPredicate,
                activeActivityPredicate, enrolmentDogPredicate);
    }

    @Override
    public void restoreState(ModelState state) {
        requireNonNull(state);
        watchDog.resetData(state.watchDog());
        activeCustomerPredicate = state.customerPredicate();
        activeDogPredicate = state.dogPredicate();
        activeActivityPredicate = state.activityPredicate();
        enrolmentDogPredicate = state.enrolmentDogPredicate();
        logger.fine("Restored " + watchDog.getCustomerList().size() + " customers, "
                + watchDog.getDogList().size() + " dogs and "
                + watchDog.getActivityList().size() + " activities.");
        signalEnrolmentContextChanged();
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

    //=========== WatchDog ================================================================================

    @Override
    public void setWatchDog(ReadOnlyWatchDog watchDog) {
        this.watchDog.resetData(watchDog);
        refreshDisplayedRecords();
    }

    @Override
    public ReadOnlyWatchDog getWatchDog() {
        return watchDog;
    }

    @Override
    public Activity addActivity(ActivityName name, LocalDate date, LocalTime start, LocalTime end,
            Recurrence recurrence) {
        Activity activity = watchDog.addActivity(name, date, start, end, recurrence);
        refreshDisplayedRecords();
        return activity;
    }

    @Override
    public void setActivity(Activity target, Activity editedActivity) {
        watchDog.setActivity(target, editedActivity);
        refreshDisplayedRecords();
    }

    @Override
    public void deleteActivity(Activity target) {
        watchDog.removeActivity(target);
        refreshDisplayedRecords();
    }

    @Override
    public void showActivitiesOnOrAfter(LocalDate date) {
        requireNonNull(date);
        updateFilteredActivityList(activity -> activity.nextOccurrenceOnOrAfter(date).isPresent());
    }

    //=========== Dog Operations ============================================================================
    @Override
    public void enrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        watchDog.enrolDogs(activityId, date, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unenrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        watchDog.unenrolDogs(activityId, date, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public List<Dog> getDogsEnrolledOn(LocalDate date) {
        requireNonNull(date);
        Set<Integer> enrolledDogIds = watchDog.getEnrolmentList().stream()
                .filter(enrolment -> enrolment.getDate().equals(date))
                .map(enrolment -> enrolment.getDogId())
                .collect(Collectors.toSet());
        return watchDog.getDogList().stream()
                .filter(dog -> enrolledDogIds.contains(dog.getId()))
                .toList();
    }

    @Override
    public void linkDogs(int customerId, List<Integer> dogIds) throws LinkException {
        watchDog.linkDogs(customerId, dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unlinkDogs(List<Integer> dogIds) throws UnlinkException {
        watchDog.unlinkDogs(dogIds);
        refreshDisplayedRecords();
    }

    @Override
    public void unlinkCustomerDogs(int customerId) throws UnlinkException {
        watchDog.unlinkCustomerDogs(customerId);
        refreshDisplayedRecords();
    }

    @Override
    public boolean hasDog(Dog dog) {
        return watchDog.hasDog(dog);
    }

    @Override
    public void deleteDog(Dog dog) {
        watchDog.removeDog(dog);
        refreshDisplayedRecords();
    }

    @Override
    public void setDog(Dog target, Dog editedDog) {
        watchDog.setDog(target, editedDog);
        refreshDisplayedRecords();
    }

    @Override
    public Dog addDog(Dog dog) {
        Dog addedDog = watchDog.addDog(dog);
        refreshDisplayedRecords();
        return addedDog;
    }

    //===========Customer related methods==================================
    @Override
    public boolean hasCustomer(Customer customer) {
        requireNonNull(customer);
        return watchDog.hasCustomer(customer);
    }

    @Override
    public void deleteCustomer(Customer target) {
        watchDog.removeCustomer(target);
        refreshDisplayedRecords();
    }

    @Override
    public void addCustomer(Customer customer) {
        watchDog.addCustomer(customer);
        refreshDisplayedRecords();
    }

    @Override
    public void setCustomer(Customer target, Customer editedCustomer) {
        requireAllNonNull(target, editedCustomer);

        watchDog.setCustomer(target, editedCustomer);
        refreshDisplayedRecords();
    }

    //=========== Filtered Record List Accessors =============================================================

    @Override
    public ObservableList<Dog> getDogList() {
        return watchDog.getDogList();
    }

    @Override
    public ObservableList<Dog> getFilteredDogList() {
        return filteredDogs;
    }

    @Override
    public void updateFilteredDogList(Predicate<Dog> predicate) {
        requireNonNull(predicate);
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
        activeActivityPredicate = predicate;
        filteredActivities.setPredicate(predicate);
        enrolmentDogPredicate = null;
        signalEnrolmentContextChanged();
    }

    @Override
    public void setActivityEnrolmentDogPredicate(Predicate<Dog> predicate) {
        enrolmentDogPredicate = predicate;
        signalEnrolmentContextChanged();
    }

    @Override
    public javafx.beans.Observable getActivityEnrolmentContextChanges() {
        return enrolmentContextVersion;
    }

    private void signalEnrolmentContextChanged() {
        enrolmentContextVersion.set(enrolmentContextVersion.get() + 1);
    }

    @Override
    public String getActivityEnrolmentSummary(Activity activity) {
        requireNonNull(activity);
        if (enrolmentDogPredicate == null) {
            return "";
        }
        List<Dog> matchingDogs = watchDog.getDogList().stream()
                .filter(enrolmentDogPredicate)
                .sorted(java.util.Comparator.comparingInt(Dog::getId))
                .toList();
        StringJoiner summary = new StringJoiner("\n", "Enrolments:\n", "");
        matchingDogs.forEach(dog -> {
            List<LocalDate> dates = watchDog.getEnrolmentList().stream()
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

    /** Reapplies active filters after stored records or relationships change. */
    private void refreshDisplayedRecords() {
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return watchDog.equals(otherModelManager.watchDog)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredCustomers.equals(otherModelManager.filteredCustomers)
                && filteredDogs.equals(otherModelManager.filteredDogs)
                && filteredActivities.equals(otherModelManager.filteredActivities);
    }

}
