package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.UniqueCustomerList;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.UniqueDogList;
import seedu.address.model.dog.exceptions.EnrolException;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;

/**
 * Wraps all data at the address-book level.
 * Stores customers and dogs with unique persistent identities.
 */
public class AddressBook implements ReadOnlyAddressBook {
    private static final Logger logger = LogsCenter.getLogger(AddressBook.class);

    private final UniqueDogList dogs = new UniqueDogList();
    private final UniqueCustomerList customers = new UniqueCustomerList();
    private final ActivityList activities = new ActivityList();
    private final ObservableList<Activity> activityView = FXCollections.unmodifiableObservableList(activities);
    private final ObservableList<Enrolment> enrolments = FXCollections.observableArrayList();
    private long nextActivityId = 1;
    private long nextDogId = 1;
    private int nextCustomerId = 1;


    /** Creates an empty address book. */
    public AddressBook() {
    }

    /**
     * Creates an AddressBook using the records in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     * Rejects dog owners missing from the incoming customers before changing any records.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        List<Activity> restored = List.copyOf(newData.getActivityList());
        List<Enrolment> restoredEnrolments = List.copyOf(newData.getEnrolmentList());
        List<Dog> restoredDogs = List.copyOf(newData.getDogList());
        List<Customer> restoredCustomers = List.copyOf(newData.getCustomerList());
        validateDogOwners(restoredDogs, restoredCustomers);
        long minimumId = restored.stream().mapToLong(Activity::getId).max().orElse(0) + 1;
        long nextId = newData.getNextActivityId();
        if (restored.stream().map(Activity::getId).distinct().count() != restored.size()
                || nextId < minimumId || nextId > (long) Integer.MAX_VALUE + 1) {
            throw new IllegalArgumentException("Invalid activity IDs or next activity ID.");
        }
        long minimumDogId = restoredDogs.stream().mapToLong(Dog::getId).max().orElse(0) + 1;
        long restoredNextDogId = newData.getNextDogId();
        long nextRestoredDogId = restoredNextDogId == -1 ? minimumDogId : restoredNextDogId;
        validateNextDogId(nextRestoredDogId, minimumDogId);
        dogs.setDogs(restoredDogs);
        customers.setCustomers(restoredCustomers);
        nextActivityId = nextId;
        nextDogId = nextRestoredDogId;

        int restoredNextCustomerId = newData.getNextCustomerId();
        this.nextCustomerId = restoredNextCustomerId >= 0
                ? restoredNextCustomerId
                : customers.asUnmodifiableObservableList().stream()
                        .mapToInt(Customer::getId).max().orElse(0) + 1;
        activities.restore(restored, restoredEnrolments);
    }

    /** Delays activity notifications until the corresponding enrolments have also been restored. */
    private final class ActivityList extends ModifiableObservableListBase<Activity> {
        private final List<Activity> values = new ArrayList<>();

        private void restore(List<Activity> restored, List<Enrolment> restoredEnrolments) {
            beginChange();
            try {
                setAll(restored);
                enrolments.setAll(restoredEnrolments);
            } finally {
                endChange();
            }
        }

        @Override
        public Activity get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        protected void doAdd(int index, Activity activity) {
            values.add(index, activity);
        }

        @Override
        protected Activity doSet(int index, Activity activity) {
            return values.set(index, activity);
        }

        @Override
        protected Activity doRemove(int index) {
            return values.remove(index);
        }
    }

    /** Replaces customers only if every existing dog owner remains present. */
    public void setCustomers(List<Customer> customers) {
        List<Customer> replacements = List.copyOf(customers);
        validateDogOwners(getDogList(), replacements);
        this.customers.setCustomers(replacements);
    }

    /**
     * Creates an empty schedule with a fresh persistent ID. Identical details are allowed.
     * Validates before changing records or consuming an ID.
     * @throws IllegalStateException if all supported IDs have been allocated.
     */
    public Activity addActivity(ActivityName name, LocalDate date, LocalTime start, LocalTime end,
            Recurrence recurrence) {
        if (nextActivityId > Integer.MAX_VALUE) {
            throw new IllegalStateException("No activity IDs remain available.");
        }
        Activity activity = new Activity((int) nextActivityId, name, date, start, end, recurrence);
        nextActivityId++;
        activities.add(activity);
        return activity;
    }

    /** Replaces a schedule after checking retained enrolments for conflicts. */
    public int setActivity(Activity target, Activity editedActivity) {
        requireNonNull(target);
        requireNonNull(editedActivity);
        if (target.getId() != editedActivity.getId()) {
            throw new IllegalArgumentException("Edited activity must retain its ID.");
        }
        int index = -1;
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).getId() == target.getId()) {
                index = i;
                break;
            }
        }
        if (index < 0 || !activities.get(index).equals(target)) {
            throw new IllegalArgumentException("Activity to edit does not match a stored activity.");
        }
        boolean occurrenceDatesChanged = !target.getStartDate().equals(editedActivity.getStartDate())
                || target.getRecurrence() != editedActivity.getRecurrence();
        if (!occurrenceDatesChanged) {
            validateNoActivityConflict(editedActivity);
        }
        int removedEnrolments = occurrenceDatesChanged
                ? (int) enrolments.stream().filter(enrolment -> enrolment.getActivityId() == target.getId()).count()
                : 0;
        activities.set(index, editedActivity);
        if (occurrenceDatesChanged) {
            enrolments.removeIf(enrolment -> enrolment.getActivityId() == target.getId());
        }
        return removedEnrolments;
    }

    /** Rejects an edited schedule that would overlap another activity attended by the same dog. */
    private void validateNoActivityConflict(Activity editedActivity) {
        for (Enrolment editedBooking : enrolments) {
            if (editedBooking.getActivityId() != editedActivity.getId()) {
                continue;
            }
            for (Enrolment otherBooking : enrolments) {
                if (otherBooking.getDogId() != editedBooking.getDogId()
                        || !otherBooking.getDate().equals(editedBooking.getDate())
                        || otherBooking.getActivityId() == editedActivity.getId()) {
                    continue;
                }
                Activity otherActivity = getActivityList().stream()
                        .filter(candidate -> candidate.getId() == otherBooking.getActivityId())
                        .findFirst().orElseThrow();
                if (timesOverlap(editedActivity, otherActivity)) {
                    throw new IllegalArgumentException("Dog ID " + editedBooking.getDogId()
                            + " has an overlapping booking in activity ID " + otherActivity.getId()
                            + " on " + editedBooking.getDate() + ".");
                }
            }
        }
    }

    private static boolean timesOverlap(Activity first, Activity second) {
        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }

    /** Restores a schedule with its persisted ID; rejects duplicate identities. */
    public void addActivityFromStorage(Activity activity) {
        requireNonNull(activity);
        if (activities.stream().anyMatch(existing -> existing.isSameActivity(activity))) {
            throw new IllegalArgumentException("Activities list contains duplicate activity IDs.");
        }
        nextActivityId = Math.max(nextActivityId, (long) activity.getId() + 1);
        activities.add(activity);
    }

    /** Restores a counter above all stored IDs; MAX_VALUE + 1 represents exhausted IDs. */
    public void setNextActivityId(long nextId) {
        long minimumId = activities.stream().mapToLong(Activity::getId).max().orElse(0) + 1;
        if (nextId < minimumId || nextId > (long) Integer.MAX_VALUE + 1) {
            throw new IllegalArgumentException("Next activity ID must exceed stored IDs and be within range.");
        }
        nextActivityId = nextId;
    }

    @Override
    public long getNextActivityId() {
        return nextActivityId;
    }

    @Override
    public ObservableList<Activity> getActivityList() {
        return activityView;
    }

    /** Removes an existing activity and its enrolments without reusing its ID. */
    public void removeActivity(Activity activity) {
        requireNonNull(activity);
        if (!activities.contains(activity)) {
            throw new IllegalArgumentException("Activity ID " + activity.getId() + " does not exist or has changed.");
        }
        enrolments.removeIf(enrolment -> enrolment.getActivityId() == activity.getId());
        activities.remove(activity);
    }

    /** Returns true if a customer with the same identity as {@code customer} exists. */
    public boolean hasCustomer(Customer customer) {
        requireNonNull(customer);
        return customers.contains(customer);
    }

    /**
     * Adds a customer, assigning a new ID when the customer is unassigned.
     * IDs increase monotonically and are never reused after deletion.
     */
    public Customer addCustomer(Customer customer) {
        requireNonNull(customer);
        Customer identifiedCustomer = customer.getId() < 0
                ? new Customer(nextCustomerId, customer.getName(), customer.getPhone(), customer.getEmail(),
                        customer.getBackupContact())
                : customer;
        customers.add(identifiedCustomer);
        nextCustomerId = Math.max(nextCustomerId, identifiedCustomer.getId() + 1);
        return identifiedCustomer;
    }

    /** Replaces customer details while preserving the persistent ID used by dog ownership links. */
    public void setCustomer(Customer target, Customer editedCustomer) {
        requireNonNull(editedCustomer);
        if (target.getId() != editedCustomer.getId()) {
            throw new IllegalArgumentException("Editing a customer must preserve its persistent ID.");
        }

        customers.setCustomer(target, editedCustomer);
    }

    /**
     * Removes {@code key} and clears ownership of that customer's dogs without deleting them.
     * {@code key} must exist in the address book. Other customers and dogs are unaffected.
     */
    public void removeCustomer(Customer key) {
        customers.remove(key);
        dogs.setDogs(getDogList().stream()
                .map(dog -> dog.getOwnerId().filter(id -> id == key.getId()).isPresent()
                        ? dog.withoutOwner() : dog)
                .toList());
    }

    /**
     * Enrols a batch of existing dogs identified by distinct positive IDs into one occurrence of an existing
     * activity atomically.
     *
     * @throws EnrolException if the date, IDs, recurrence, duplicate, or time-conflict checks fail
     */
    public void enrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        requireNonNull(date);
        requireNonNull(dogIds);
        validateDogIdBatch(dogIds);
        if (date.isBefore(LocalDate.now())) {
            throw new EnrolException("Cannot enrol on " + date + ": the date must be today or later. Today is "
                    + LocalDate.now() + ".");
        }
        Activity activity = getActivity(activityId);
        if (!activity.occursOn(date)) {
            throw occurrenceError(activity, date);
        }
        for (int id : dogIds) {
            validateDogExists(id);
            validateEnrolment(new Enrolment(activityId, id, date), activity);
        }
        for (int id : dogIds) {
            enrolments.add(new Enrolment(activityId, id, date));
        }
        assert dogIds.stream().allMatch(id -> enrolments.contains(new Enrolment(activityId, id, date)));
        logger.fine("Enrolled dog IDs " + dogIds + " in activity ID " + activityId + " on " + date + ".");
    }

    /**
     * Removes a batch of existing dogs identified by distinct positive IDs from one occurrence of an existing
     * activity atomically.
     *
     * @throws EnrolException if the date, IDs, recurrence, or enrolment checks fail
     */
    public void unenrolDogs(int activityId, LocalDate date, List<Integer> dogIds) throws EnrolException {
        requireNonNull(date);
        requireNonNull(dogIds);
        validateDogIdBatch(dogIds);
        Activity activity = getActivity(activityId);
        if (!activity.occursOn(date)) {
            throw occurrenceError(activity, date);
        }
        for (int dogId : dogIds) {
            validateDogExists(dogId);
            if (enrolments.stream().noneMatch(enrolment ->
                    enrolment.equals(new Enrolment(activityId, dogId, date)))) {
                throw new EnrolException("Dog ID " + dogId + " is not enrolled in activity ID " + activityId
                        + " on " + date + ".");
            }
        }
        enrolments.removeIf(enrolment -> enrolment.getActivityId() == activityId
                && enrolment.getDate().equals(date) && dogIds.contains(enrolment.getDogId()));
        assert dogIds.stream().noneMatch(dogId ->
                enrolments.contains(new Enrolment(activityId, dogId, date)));
        logger.fine("Unenrolled dog IDs " + dogIds + " from activity ID " + activityId + " on " + date + ".");
    }

    /**
     * Links dogs identified by distinct positive IDs to an existing customer, regardless of display filters.
     * Validates the entire batch before replacing the dog list. Rejected batches change nothing.
     *
     * @throws LinkException if IDs repeat, endpoints are missing, or any dog already has an owner.
     */
    public void linkDogs(int customerId, List<Integer> dogIds) throws LinkException {
        requireNonNull(dogIds);
        if (dogIds.isEmpty() || dogIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(dogIds).size() != dogIds.size()) {
            throw new LinkException("Specify at least one dog ID, with no repeated IDs.");
        }
        if (dogIds.stream().anyMatch(dogId -> dogId < 1)) {
            throw new LinkException("Dog IDs must be positive.");
        }
        if (getCustomerList().stream().noneMatch(customer -> customer.getId() == customerId)) {
            throw new LinkException("Customer ID " + customerId + " does not exist.");
        }
        Map<Integer, Dog> replacements = new HashMap<>();
        for (int dogId : dogIds) {
            Dog dog = getDogList().stream().filter(candidate -> candidate.getId() == dogId).findFirst()
                    .orElseThrow(() -> new LinkException("Dog ID " + dogId + " does not exist."));
            if (dog.getOwnerId().isPresent()) {
                throw new LinkException("Dog ID " + dogId + " is already linked to customer ID "
                        + dog.getOwnerId().get() + ".");
            }
            replacements.put(dogId, dog.withOwner(customerId));
        }
        dogs.setDogs(getDogList().stream().map(dog -> replacements.getOrDefault(dog.getId(), dog)).toList());
        // All endpoints were validated; a mismatch here means the internal update is defective.
        assert hasExpectedOwners(dogIds, customerId) : "Linked dogs must exist and belong to the requested customer.";
    }

    /**
     * Unlinks dogs identified by distinct positive IDs from their current owners without deleting records or
     * changing filters.
     * Validates every endpoint and ownership before publishing the updated dog list.
     *
     * @throws UnlinkException if the batch is empty, IDs repeat, dogs are missing,
     *     or any dog has no owner.
     */
    public void unlinkDogs(List<Integer> dogIds) throws UnlinkException {
        requireNonNull(dogIds);
        if (dogIds.isEmpty() || dogIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(dogIds).size() != dogIds.size()) {
            throw new UnlinkException("Specify at least one dog ID, with no repeated IDs.");
        }
        if (dogIds.stream().anyMatch(dogId -> dogId < 1)) {
            throw new UnlinkException("Dog IDs must be positive.");
        }
        Map<Integer, Dog> replacements = new HashMap<>();
        for (int dogId : dogIds) {
            Dog dog = getDogList().stream().filter(candidate -> candidate.getId() == dogId).findFirst()
                    .orElseThrow(() -> new UnlinkException("Dog ID " + dogId + " does not exist."));
            if (dog.getOwnerId().isEmpty()) {
                throw new UnlinkException("Dog ID " + dogId + " has no owner.");
            }
            replacements.put(dogId, dog.withoutOwner());
        }
        dogs.setDogs(getDogList().stream().map(dog -> replacements.getOrDefault(dog.getId(), dog)).toList());
        assert hasExpectedOwners(dogIds, null) : "Unlinked dogs must exist and have no owner.";
    }

    /** Checks stored endpoints as well as ownership, so missing dogs cannot satisfy the postcondition. */
    private boolean hasExpectedOwners(List<Integer> dogIds, Integer customerId) {
        return dogIds.stream().allMatch(dogId -> getDogList().stream()
                .anyMatch(dog -> dog.getId() == dogId
                        && Objects.equals(dog.getOwnerId().orElse(null), customerId)));
    }

    /** Rejects dangling ownership references before publishing replacements or restored records. */
    private static void validateDogOwners(List<Dog> dogs, List<Customer> customers) {
        Set<Integer> customerIds = new HashSet<>();
        customers.forEach(customer -> customerIds.add(customer.getId()));
        for (Dog dog : dogs) {
            if (dog.getOwnerId().filter(ownerId -> !customerIds.contains(ownerId)).isPresent()) {
                throw new IllegalArgumentException("Dog owner must refer to an existing customer.");
            }
        }
    }

    /**
     * Unlinks all dogs owned by a customer, including dogs hidden by filters.
     * @throws UnlinkException if the customer is missing or has no linked dogs.
     */
    public void unlinkCustomerDogs(int customerId) throws UnlinkException {
        if (getCustomerList().stream().noneMatch(customer -> customer.getId() == customerId)) {
            throw new UnlinkException("Customer ID " + customerId + " does not exist.");
        }
        List<Integer> dogIds = getDogList().stream()
                .filter(dog -> dog.getOwnerId().filter(ownerId -> ownerId == customerId).isPresent())
                .map(Dog::getId).toList();
        if (dogIds.isEmpty()) {
            throw new UnlinkException("Customer ID " + customerId + " has no linked dogs.");
        }
        unlinkDogs(dogIds);
    }

    /**
     * Adds a new dog and assigns it the next available persistent ID.
     * The counter is consumed only after the dog is stored successfully.
     *
     * @param dog dog details without an assigned ID
     * @return the identified dog that was stored
     * @throws IllegalStateException if all positive {@code int} dog IDs have been allocated
     */
    public Dog addDog(Dog dog) {
        requireNonNull(dog);
        if (nextDogId > Integer.MAX_VALUE) {
            throw new IllegalStateException("No more dog IDs are available.");
        }
        Dog newDog = new Dog((int) nextDogId, dog.getName(), dog.getBreed(), dog.getSize());
        dogs.add(newDog);
        nextDogId++;
        return newDog;
    }

    /**
     * Adds a dog loaded from storage without assigning it a new ID.
     *
     * @param identified dog with its positive persisted ID
     * @return the stored dog
     * @throws IllegalArgumentException if the dog does not have a positive persistent ID
     */
    public Dog addDogFromStorage(Dog identified) {
        requireNonNull(identified);
        validateDogOwners(List.of(identified), getCustomerList());
        dogs.add(identified);
        nextDogId = Math.max(nextDogId, (long) identified.getId() + 1);
        return identified;
    }

    @Override
    public long getNextDogId() {
        return nextDogId;
    }

    @Override
    public int getNextCustomerId() {
        return nextCustomerId;
    }

    /**
     * Restores a counter above all stored dog IDs. {@code Integer.MAX_VALUE + 1} represents exhaustion.
     *
     * @throws IllegalArgumentException if {@code nextDogId} does not exceed all stored IDs or is out of range
     */
    public void setNextDogId(long nextDogId) {
        long minimumDogId = getDogList().stream().mapToLong(Dog::getId).max().orElse(0) + 1;
        validateNextDogId(nextDogId, minimumDogId);
        this.nextDogId = nextDogId;
    }

    private static void validateNextDogId(long nextDogId, long minimumDogId) {
        if (nextDogId < minimumDogId || nextDogId > (long) Integer.MAX_VALUE + 1) {
            throw new IllegalArgumentException(
                    "Next dog ID must be positive, exceed all stored dog IDs, and be within range.");
        }
    }

    /** Restores the next unused customer ID from persistent storage. */
    public void setNextCustomerId(int nextCustomerId) {
        if (nextCustomerId < 1) {
            throw new IllegalArgumentException("Next customer ID must be positive.");
        }
        this.nextCustomerId = nextCustomerId;
    }

    /**
     * Returns true if a stored dog has the same persistent ID as {@code dog}.
     * Unassigned dogs do not have an identity and therefore never match.
     *
     * @param dog dog to check
     * @return whether a dog with the same ID exists
     */
    public boolean hasDog(Dog dog) {
        requireNonNull(dog);
        return dogs.contains(dog);
    }

    /**
     * Removes the given dog and all of its enrolments, including enrolments for past activity occurrences.
     *
     * @throws IllegalArgumentException if no stored dog has the same persistent ID
     */
    public void removeDog(Dog dog) {
        requireNonNull(dog);
        Dog storedDog = getDogList().stream()
                .filter(candidate -> candidate.isSameDog(dog))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dog does not exist."));
        int dogId = storedDog.getId();
        long removedEnrolmentCount = enrolments.stream().filter(enrolment -> enrolment.getDogId() == dogId).count();
        enrolments.removeIf(enrolment -> enrolment.getDogId() == dogId);
        dogs.remove(storedDog);
        assert enrolments.stream().noneMatch(enrolment -> enrolment.getDogId() == dogId);
        assert getDogList().stream().noneMatch(remainingDog -> remainingDog.isSameDog(storedDog));
        logger.fine("Deleted dog ID " + dogId + " and removed " + removedEnrolmentCount + " enrolments.");
    }

    /** Replaces an existing dog after validating that any owner refers to an existing customer. */
    public void setDog(Dog target, Dog editedDog) {
        validateDogOwners(List.of(editedDog), getCustomerList());
        dogs.setDog(target, editedDog);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dogs", dogs)
                .add("customers", customers)
                .toString();
    }

    @Override
    public ObservableList<Dog> getDogList() {
        return dogs.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Customer> getCustomerList() {
        return customers.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Enrolment> getEnrolmentList() {
        return FXCollections.unmodifiableObservableList(enrolments);
    }

    /** Adds a validated enrolment restored from storage. */
    public void addEnrolmentFromStorage(Enrolment enrolment) {
        requireNonNull(enrolment);
        try {
            Activity activity = getActivity(enrolment.getActivityId());
            validateDogExists(enrolment.getDogId());
            validateEnrolment(enrolment, activity);
        } catch (EnrolException e) {
            logger.warning("Rejected stored enrolment (activity ID " + enrolment.getActivityId() + ", dog ID "
                    + enrolment.getDogId() + ", date " + enrolment.getDate() + "): " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        enrolments.add(enrolment);
    }

    private Activity getActivity(int activityId) throws EnrolException {
        if (activityId < 1) {
            throw new EnrolException("Activity IDs must be positive whole numbers.");
        }
        return getActivityList().stream().filter(activity -> activity.getId() == activityId).findFirst()
                .orElseThrow(() -> new EnrolException("Activity ID " + activityId + " does not exist."));
    }

    private void validateDogExists(int dogId) throws EnrolException {
        if (dogId < 1 || getDogList().stream().noneMatch(dog -> dog.getId() == dogId)) {
            throw new EnrolException("Dog ID " + dogId + " does not exist.");
        }
    }

    private void validateDogIdBatch(List<Integer> dogIds) throws EnrolException {
        if (dogIds.isEmpty()) {
            throw new EnrolException("Provide at least one dog ID.");
        }
        if (dogIds.stream().anyMatch(Objects::isNull)) {
            throw new EnrolException("Dog IDs must not be null.");
        }
        HashSet<Integer> seen = new HashSet<>();
        for (int dogId : dogIds) {
            if (dogId < 1) {
                throw new EnrolException("Dog IDs must be positive whole numbers.");
            }
            if (!seen.add(dogId)) {
                throw new EnrolException("Dog ID " + dogId
                        + " is listed more than once. Provide each dog ID only once.");
            }
        }
    }

    private EnrolException occurrenceError(Activity activity, LocalDate date) {
        if (date.isBefore(activity.getStartDate())) {
            return new EnrolException("Activity ID " + activity.getId() + " starts on " + activity.getStartDate()
                    + " and does not occur before that date.");
        }
        if (activity.getRecurrence() == Recurrence.WEEKLY) {
            return new EnrolException("Activity ID " + activity.getId() + " recurs weekly on "
                    + activity.getStartDate().getDayOfWeek() + ", starting " + activity.getStartDate()
                    + "; it does not occur on " + date + ".");
        }
        // Daily schedules occur on every date from their start date, so only one-time schedules remain here.
        return new EnrolException("Activity ID " + activity.getId() + " is a one-time activity and "
                + "occurs only on " + activity.getStartDate() + ", not " + date + ".");
    }

    private void validateEnrolment(Enrolment enrolment, Activity activity) throws EnrolException {
        LocalDate date = enrolment.getDate();
        if (!activity.occursOn(date)) {
            throw occurrenceError(activity, date);
        }
        if (enrolments.contains(enrolment)) {
            throw new EnrolException("Dog ID " + enrolment.getDogId() + " is already enrolled in activity "
                    + activity.getId() + " on " + date + ".");
        }
        for (Enrolment existing : enrolments) {
            if (existing.getDogId() != enrolment.getDogId() || !existing.getDate().equals(date)) {
                continue;
            }
            Activity existingActivity = getActivity(existing.getActivityId());
            if (activity.getStartTime().isBefore(existingActivity.getEndTime())
                    && existingActivity.getStartTime().isBefore(activity.getEndTime())) {
                throw new EnrolException("Dog ID " + enrolment.getDogId() + " is already enrolled in \""
                        + existingActivity.getName() + "\" (activity " + existingActivity.getId() + ", "
                        + existingActivity.getStartTime() + "-" + existingActivity.getEndTime() + ") on " + date
                        + "; this overlaps \"" + activity.getName() + "\" (activity " + activity.getId() + ", "
                        + activity.getStartTime() + "-" + activity.getEndTime() + ").");
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return dogs.equals(otherAddressBook.dogs)
                && customers.equals(otherAddressBook.customers)
                && activities.equals(otherAddressBook.activities)
                && enrolments.equals(otherAddressBook.enrolments)
                && nextDogId == otherAddressBook.nextDogId
                && nextCustomerId == otherAddressBook.nextCustomerId
                && nextActivityId == otherAddressBook.nextActivityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dogs, customers, activities, enrolments,
                nextDogId, nextCustomerId, nextActivityId);
    }
}
