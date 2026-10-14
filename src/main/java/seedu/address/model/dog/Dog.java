package seedu.address.model.dog;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a dog in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Dog {

    private final int id;
    private final DogName name;
    private final Breed breed;
    private final Size size;

    /**
     * Creates a dog with the given details.
     *
     * @param name name of the dog
     * @param breed breed of the dog
     * @param size size of the dog
     */
    public Dog(int id, DogName name, Breed breed, Size size) {
        requireAllNonNull(name, breed, size);
        if (id < 0) {
            throw new IllegalArgumentException("Dog id must be nonnegative.");
        }
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.size = size;
    }


    /**
     * Creates a dog without an assigned persistent ID.
     * This constructor is retained temporarily for existing parser/tests;
     * new dogs should receive their ID through the model/address book.
     */
    public Dog(DogName name, Breed breed, Size size) {
        requireAllNonNull(name, breed, size);
        this.id = -1; //unassigned id
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
                && size.equals(otherDog.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, breed, size);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("breed", breed)
                .add("size", size)
                .toString();
    }
}
