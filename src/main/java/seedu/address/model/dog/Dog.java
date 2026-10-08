package seedu.address.model.dog;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Dog {

    private final DogName name;
    private final Breed breed;
    private final Size size;

    /**
     * Every field must be present and not null.
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

        return otherDog != null
                && otherDog.getName().equals(getName());
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

        // instanceof handles nulls
        if (!(other instanceof Dog otherDog)) {
            return false;
        }

        return name.equals(otherDog.name)
                && breed.equals(otherDog.breed)
                && size.equals(otherDog.size);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
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
