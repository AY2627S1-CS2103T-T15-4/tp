package seedu.address.model.dog;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a dog in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Dog {

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
    public Dog(DogName name, Breed breed, Size size) {
        requireAllNonNull(name, breed, size);
        this.name = name;
        this.breed = breed;
        this.size = size;
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
     * Returns true if both dogs have the same name.
     * This defines a weaker notion of equality between two dogs.
     */
    public boolean isSameDog(Dog otherDog) {
        if (otherDog == this) {
            return true;
        }
        return otherDog != null && otherDog.getName().equals(getName());
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
        return name.equals(otherDog.name)
                && breed.equals(otherDog.breed)
                && size.equals(otherDog.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, breed, size);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("breed", breed)
                .add("size", size)
                .toString();
    }
}
