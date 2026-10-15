package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.UniqueCustomerList;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.UniqueDogList;
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
        this.nextDogId = 1;
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

    public void setCustomer(Customer target, Customer editedCustomer) {
        requireNonNull(editedCustomer);

        customers.setCustomer(target, editedCustomer);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removeCustomer(Customer key) {
        customers.remove(key);
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
