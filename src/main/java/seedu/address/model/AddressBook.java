package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.UniqueCustomerList;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.UniqueDogList;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueDogList dogs = new UniqueDogList();
    private final UniqueCustomerList customers = new UniqueCustomerList();
    private int nextDogId;
    private int nextCustomerId;

    /** Creates an empty address book. */
    public AddressBook() {
        this.nextDogId = 0;
        this.nextCustomerId = 1;
    }

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        dogs.setDogs(newData.getDogList());
        setCustomers(newData.getCustomerList());

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

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
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
     * Unlinks a batch from an existing customer without deleting records or changing filters.
     * Validates every endpoint and ownership before publishing the updated dog list.
     *
     * @throws UnlinkException if the batch is empty, IDs repeat, endpoints are missing,
     *     or any dog is not owned by the specified customer.
     */
    public void unlinkDogs(int customerId, List<Integer> dogIds) throws UnlinkException {
        requireNonNull(dogIds);
        if (dogIds.isEmpty() || dogIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(dogIds).size() != dogIds.size()) {
            throw new UnlinkException("Specify at least one dog ID, with no repeated IDs.");
        }
        if (getCustomerList().stream().noneMatch(customer -> customer.getId() == customerId)) {
            throw new UnlinkException("Customer ID " + customerId + " does not exist.");
        }
        Map<Integer, Dog> replacements = new HashMap<>();
        for (int dogId : dogIds) {
            Dog dog = getDogList().stream().filter(candidate -> candidate.getId() == dogId).findFirst()
                    .orElseThrow(() -> new UnlinkException("Dog ID " + dogId + " does not exist."));
            if (dog.getOwnerId().filter(ownerId -> ownerId == customerId).isEmpty()) {
                throw new UnlinkException("Dog ID " + dogId + " is not linked to customer ID " + customerId + ".");
            }
            replacements.put(dogId, dog.withoutOwner());
        }
        dogs.setDogs(getDogList().stream().map(dog -> replacements.getOrDefault(dog.getId(), dog)).toList());
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

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("customers", customers)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
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
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && dogs.equals(otherAddressBook.dogs)
                && customers.equals(otherAddressBook.customers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, dogs, customers);
    }
}
