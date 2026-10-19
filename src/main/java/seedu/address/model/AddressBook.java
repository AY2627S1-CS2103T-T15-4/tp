package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private final UniqueDogList dogs = new UniqueDogList();
    private final UniqueCustomerList customers = new UniqueCustomerList();
    private final ObservableList<Activity> activities = FXCollections.observableArrayList();
    private final ObservableList<Activity> activityView = FXCollections.unmodifiableObservableList(activities);
    private final ObservableList<Enrolment> enrolments = FXCollections.observableArrayList();
    private long nextActivityId = 1;
    private int nextDogId = 1;
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
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        List<Activity> restored = List.copyOf(newData.getActivityList());
        long minimumId = restored.stream().mapToLong(Activity::getId).max().orElse(0) + 1;
        long nextId = newData.getNextActivityId();
        if (restored.stream().map(Activity::getId).distinct().count() != restored.size()
                || nextId < minimumId || nextId > (long) Integer.MAX_VALUE + 1) {
            throw new IllegalArgumentException("Invalid activity IDs or next activity ID.");
        }
        dogs.setDogs(newData.getDogList());
        setCustomers(newData.getCustomerList());
        nextActivityId = nextId;
        activities.setAll(restored);
        enrolments.setAll(newData.getEnrolmentList());

        int restoredNextDogId = newData.getNextDogId();
        this.nextDogId = restoredNextDogId >= 0
                ? restoredNextDogId
                : dogs.asUnmodifiableObservableList().stream()
                        .mapToInt(Dog::getId).max().orElse(-1) + 1;

        int restoredNextCustomerId = newData.getNextCustomerId();
        this.nextCustomerId = restoredNextCustomerId >= 0
                ? restoredNextCustomerId
                : customers.asUnmodifiableObservableList().stream()
                        .mapToInt(Customer::getId).max().orElse(0) + 1;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers.setCustomers(customers);
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

    /** Restores a schedule with its persisted ID; rejects duplicate identities. */
    public void addActivityFromStorage(Activity activity) {
        requireNonNull(activity);
        if (activities.stream().anyMatch(existing -> existing.isSameActivity(activity))) {
            throw new IllegalArgumentException("Activities list contains duplicate activity IDs.");
        }
        nextActivityId = Math.max(nextActivityId, (long) activity.getId() + 1);
        activities.add(activity);
    }

    /** Marks the activity with the specified ID as completed. */
    public void markActivity(int activityId) {
        replaceActivity(activityId, Activity::markCompleted);
    }

    /** Marks the activity with the specified ID as incomplete. */
    public void unmarkActivity(int activityId) {
        replaceActivity(activityId, Activity::markIncomplete);
    }

    private void replaceActivity(int activityId, java.util.function.UnaryOperator<Activity> replacement) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).getId() == activityId) {
                activities.set(i, replacement.apply(activities.get(i)));
                return;
            }
        }
        throw new IllegalArgumentException("Activity ID " + activityId + " does not exist.");
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
     * Enrols a batch of existing dogs into an existing activity atomically.
     *
     * @throws EnrolException if the IDs are invalid or an endpoint is missing
     */
    public void enrolDogs(int activityId, List<Integer> dogIds) throws EnrolException {
        requireNonNull(dogIds);
        if (dogIds.isEmpty() || dogIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(dogIds).size() != dogIds.size()) {
            throw new EnrolException("Specify at least one dog ID, with no repeated IDs.");
        }
        if (getActivityList().stream().noneMatch(activity -> activity.getId() == activityId)) {
            throw new EnrolException("Activity ID " + activityId + " does not exist.");
        }
        for (int id : dogIds) {
            getDogList().stream().filter(candidate -> candidate.getId() == id).findFirst()
                    .orElseThrow(() -> new EnrolException("Dog ID " + id + " does not exist."));
            if (enrolments.stream().anyMatch(enrolment -> enrolment.equals(new Enrolment(activityId, id)))) {
                throw new EnrolException("Dog ID " + id + " is already enrolled in activity ID " + activityId + ".");
            }
        }
        for (int id : dogIds) {
            enrolments.add(new Enrolment(activityId, id));
        }
    }

    /**
     * Removes a batch of existing dogs from an existing activity atomically.
     *
     * @throws EnrolException if the IDs are invalid or an enrolment is missing
     */
    public void unenrolDogs(int activityId, List<Integer> dogIds) throws EnrolException {
        requireNonNull(dogIds);
        if (dogIds.isEmpty() || dogIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(dogIds).size() != dogIds.size()) {
            throw new EnrolException("Specify at least one dog ID, with no repeated IDs.");
        }
        if (getActivityList().stream().noneMatch(activity -> activity.getId() == activityId)) {
            throw new EnrolException("Activity ID " + activityId + " does not exist.");
        }
        for (int dogId : dogIds) {
            if (getDogList().stream().noneMatch(dog -> dog.getId() == dogId)) {
                throw new EnrolException("Dog ID " + dogId + " does not exist.");
            }
            if (enrolments.stream().noneMatch(enrolment ->
                    enrolment.equals(new Enrolment(activityId, dogId)))) {
                throw new EnrolException("Dog ID " + dogId + " is not enrolled in activity ID " + activityId + ".");
            }
        }
        enrolments.removeIf(enrolment -> enrolment.getActivityId() == activityId
                && dogIds.contains(enrolment.getDogId()));
    }

    /**
     * Links all requested dogs to an existing customer, regardless of display filters.
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
    }

    /**
     * Unlinks a batch from their current owners without deleting records or changing filters.
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
     *
     * @param dog dog details without an assigned ID
     * @return the identified dog that was stored
     */
    public Dog addDog(Dog dog) {
        requireNonNull(dog);
        Dog newDog = new Dog(nextDogId, dog.getName(), dog.getBreed(), dog.getSize());
        dogs.add(newDog);
        nextDogId++;
        return newDog;
    }

    /**
     * Adds a dog loaded from storage without assigning it a new ID.
     *
     * @param identified dog with its persisted ID
     * @return the stored dog
     */
    public Dog addDogFromStorage(Dog identified) {
        requireNonNull(identified);
        if (identified.getOwnerId().isPresent() && getCustomerList().stream()
                .noneMatch(customer -> customer.getId() == identified.getOwnerId().get())) {
            throw new IllegalArgumentException("Dog owner must refer to an existing customer.");
        }
        dogs.add(identified);
        nextDogId = Math.max(nextDogId, identified.getId() + 1);
        return identified;
    }

    @Override
    public int getNextDogId() {
        return nextDogId;
    }

    @Override
    public int getNextCustomerId() {
        return nextCustomerId;
    }

    /** Restores the next unused dog ID from persistent storage. */
    public void setNextDogId(int nextDogId) {
        if (nextDogId < 0) {
            throw new IllegalArgumentException("Next dog ID must be nonnegative.");
        }
        this.nextDogId = nextDogId;
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

    /** Removes the given dog from this address book. */
    public void removeDog(Dog dog) {
        requireNonNull(dog);
        dogs.remove(dog);
    }

    /** Replaces an existing dog with its edited version. */
    public void setDog(Dog target, Dog editedDog) {
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
        if (enrolments.contains(enrolment)) {
            throw new IllegalArgumentException("Enrolments list contains duplicate enrolments.");
        }
        enrolments.add(enrolment);
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
                && nextActivityId == otherAddressBook.nextActivityId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dogs, customers, activities, enrolments, nextActivityId);
    }
}
