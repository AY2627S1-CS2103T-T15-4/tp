package seedu.address.model.dog;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a dog in the address book.
 * Guarantees: stored dogs have positive IDs; details are present and not null, field values are validated, immutable.
 */
public class Dog {

    private final int id;
    private final DogName name;
    private final Breed breed;
    private final Size size;
    private final Integer ownerId;

    /**
     * Creates a dog with the given details.
     *
     * @param id Positive persistent dog ID.
     * @param name Name of the dog.
     * @param breed Breed of the dog.
     * @param size Size of the dog.
     */
    public Dog(int id, DogName name, Breed breed, Size size) {
        this(id, name, breed, size, null);
    }

    /** Creates a stored dog with a positive dog ID and an optional positive owner ID. */
    public Dog(int id, DogName name, Breed breed, Size size, Integer ownerId) {
        requireAllNonNull(name, breed, size);
        if (id < 1) {
            throw new IllegalArgumentException("Dog id must be positive.");
        }
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.size = size;
        if (ownerId != null && ownerId < 1) {
            throw new IllegalArgumentException("Owner ID must be positive.");
        }
        this.ownerId = ownerId;
    }


    /**
     * Creates a dog without an assigned persistent ID.
     * This constructor is retained temporarily for existing parser/tests;
     * new dogs should receive their ID through the model/address book.
     */
    public Dog(DogName name, Breed breed, Size size) {
        requireAllNonNull(name, breed, size);
        this.id = -1; //unassigned id
        this.ownerId = null;
        this.name = name;
        this.breed = breed;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public DogName getName() {
        return name;
    }

    public Breed getBreed() {
        return breed;
    }

    public Size getSize() {
        return size;
    }

    public Optional<Integer> getOwnerId() {
        return Optional.ofNullable(ownerId);
    }

    /** Returns a copy with the same identity and details and the specified owner. */
    public Dog withOwner(int customerId) {
        return new Dog(id, name, breed, size, customerId);
    }

    /** Returns an unowned copy with the same persistent ID and details. */
    public Dog withoutOwner() {
        return new Dog(id, name, breed, size);
    }

    /**
     * Returns true if both dogs have the same ID.
     * This defines a weaker notion of equality between two dogs.
     */
    public boolean isSameDog(Dog otherDog) {
        if (otherDog == this) {
            return true;
        }
        return otherDog != null && otherDog.getId() == getId();
    }

    /**
     * Returns true if both dogs have the same identity and data fields.
     * This defines a stronger notion of equality between two dogs.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Dog otherDog)) {
            return false;
        }
        return id == otherDog.id
                && name.equals(otherDog.name)
                && breed.equals(otherDog.breed)
                && size.equals(otherDog.size)
                && Objects.equals(ownerId, otherDog.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, breed, size, ownerId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("breed", breed)
                .add("size", size)
                .add("ownerId", ownerId)
                .toString();
    }
}
