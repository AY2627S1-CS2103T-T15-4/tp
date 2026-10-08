package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;

/** Represents a dog in the address book. */
public class Dog {
    private final int id;
    private final String name;
    private final String breed;
    private final String size;

    /**
     * Creates a dog with the given details.
     *
     * @param id unique identifier of the dog
     * @param name name of the dog
     * @param breed breed of the dog
     * @param size size of the dog
     */
    public Dog(int id, String name, String breed, String size) {
        requireNonNull(name);
        requireNonNull(breed);
        requireNonNull(size);
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getSize() {
        return size;
    }

    @Override
    public String toString() {
        return String.format("%d. %s; Breed: %s; Size: %s", id, name, breed, size);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Dog dog
                && id == dog.id && name.equals(dog.name) && breed.equals(dog.breed) && size.equals(dog.size));
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, breed, size);
    }
}
