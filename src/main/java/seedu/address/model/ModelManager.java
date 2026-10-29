package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
// @@author XanderGoh
import java.util.StringJoiner;
// @@author
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// @@author XanderGoh
import javafx.beans.property.SimpleIntegerProperty;
// @@author
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
// @@author XanderGoh
import seedu.address.model.activity.Enrolment;
// @@author
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
// @@author XanderGoh
import seedu.address.model.dog.Dog;
// @@author
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
    // @@author XanderGoh
    private Predicate<Customer> activeCustomerPredicate;
    private Predicate<Dog> activeDogPredicate;
    private Predicate<Activity> activeActivityPredicate;
    private Predicate<Dog> enrolmentDogPredicate;
    private final SimpleIntegerProperty enrolmentContextVersion = new SimpleIntegerProperty();
    // @@author

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

    //@@author its-shoutime
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
        // @@author XanderGoh
        enrolmentDogPredicate = state.enrolmentDogPredicate();
        // @@author
        logger.fine("Restored " + watchDog.getCustomerList().size() + " customers, "
                + watchDog.getDogList().size() + " dogs and "
                + watchDog.getActivityList().size() + " activities.");
        // @@author XanderGoh
        signalEnrolmentContextChanged();
        // @@author
        filteredCustomers.setPredicate(null);
        filteredDogs.setPredicate(null);
        filteredActivities.setPredicate(null);
        refreshDisplayedRecords();
        // Some restored predicates read another filtered list; re-evaluate after their source is installed.
        refreshDisplayedRecords();
    }
    //@@author

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
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
        return activity;
    }

    // @@author lilosy
    @Override
    public void setActivity(Activity target, Activity editedActivity) {
        watchDog.setActivity(target, editedActivity);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author lilosy
    }

    @Override
    public void deleteActivity(Activity target) {
        watchDog.removeActivity(target);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author lilosy
    }

    @Override
    public void showActivitiesOnOrAfter(LocalDate date) {
        requireNonNull(date);
        updateFilteredActivityList(activity -> activity.nextOccurrenceOnOrAfter(date).isPresent());
    }
    // @@author

    //=========== Dog Operations ============================================================================
    @Override
    public void enrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        watchDog.enrolDogs(activityId, date, dogIds);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }

    @Override
    public void unenrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        watchDog.unenrolDogs(activityId, date, dogIds);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
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
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }

    @Override
    public void unlinkDogs(List<Integer> dogIds) throws UnlinkException {
        watchDog.unlinkDogs(dogIds);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }

    @Override
    public void unlinkCustomerDogs(int customerId) throws UnlinkException {
        watchDog.unlinkCustomerDogs(customerId);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }

    @Override
    public boolean hasDog(Dog dog) {
        return watchDog.hasDog(dog);
    }

    // @@author XanderGoh
    @Override
    public void deleteDog(Dog dog) {
        watchDog.removeDog(dog);
        refreshDisplayedRecords();
    }
    // @@author

    @Override
    public void setDog(Dog target, Dog editedDog) {
        watchDog.setDog(target, editedDog);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }

    @Override
    public Dog addDog(Dog dog) {
        Dog addedDog = watchDog.addDog(dog);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
        return addedDog;
    }

    //===========Customer related methods==================================
    @Override
    public boolean hasCustomer(Customer customer) {
        requireNonNull(customer);
        return watchDog.hasCustomer(customer);
    }

    //@@author its-shoutime
    @Override
    public void deleteCustomer(Customer target) {
        watchDog.removeCustomer(target);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author
    }
    //@@author

    // @@author lilosy
    @Override
    public void addCustomer(Customer customer) {
        watchDog.addCustomer(customer);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author lilosy
    }

    @Override
    public void setCustomer(Customer target, Customer editedCustomer) {
        requireAllNonNull(target, editedCustomer);

        watchDog.setCustomer(target, editedCustomer);
        // @@author XanderGoh
        refreshDisplayedRecords();
        // @@author lilosy
    }
    // @@author

    //=========== Filtered Record List Accessors =============================================================

    // @@author XanderGoh
    @Override
    public ObservableList<Dog> getDogList() {
        return watchDog.getDogList();
    }
    // @@author

    @Override
    public ObservableList<Dog> getFilteredDogList() {
        return filteredDogs;
    }

    @Override
    public void updateFilteredDogList(Predicate<Dog> predicate) {
        requireNonNull(predicate);
        // @@author XanderGoh
        activeDogPredicate = predicate;
        // @@author
        filteredDogs.setPredicate(predicate);
    }

    //@@author its-shoutime
    @Override
    public ObservableList<Customer> getFilteredCustomerList() {
        return filteredCustomers;
    }

    @Override
    public void updateFilteredCustomerList(Predicate<Customer> predicate) {
        requireNonNull(predicate);
        // @@author XanderGoh
        activeCustomerPredicate = predicate;
        // @@author
        filteredCustomers.setPredicate(predicate);
    }
    //@@author

    @Override
    public ObservableList<Activity> getFilteredActivityList() {
        return filteredActivities;
    }

    @Override
    public void updateFilteredActivityList(Predicate<Activity> predicate) {
        requireNonNull(predicate);
        // @@author XanderGoh
        activeActivityPredicate = predicate;
        // @@author
        filteredActivities.setPredicate(predicate);
        // @@author XanderGoh
        enrolmentDogPredicate = null;
        signalEnrolmentContextChanged();
        // @@author
    }

    // @@author XanderGoh
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
    // @@author

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
