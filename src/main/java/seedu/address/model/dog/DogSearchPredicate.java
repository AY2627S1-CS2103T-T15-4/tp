package seedu.address.model.dog;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/** Filters dogs by name, breed, size, and ID. */
public class DogSearchPredicate implements Predicate<Dog> {
    private final String nameQuery;
    private final String breedQuery;
    private final String sizeQuery;
    private final Integer id;

    /** Creates a predicate that searches dog names by substring. */
    public DogSearchPredicate(String nameQuery) {
        this(validateName(nameQuery), null, null, null);
    }

    /** Creates a predicate that searches for one dog ID. */
    public DogSearchPredicate(int id) {
        this(null, null, null, validateId(id));
    }

    /** Creates a predicate from any combination of supported dog fields. */
    public DogSearchPredicate(String nameQuery, String breedQuery, String sizeQuery, Integer id) {
        this.nameQuery = normalize(nameQuery);
        this.breedQuery = normalize(breedQuery);
        this.sizeQuery = normalize(sizeQuery);
        this.id = id == null ? null : validateId(id);
    }

    @Override
    public boolean test(Dog dog) {
        return (nameQuery == null || normalize(dog.getName().fullName).contains(nameQuery))
                && (breedQuery == null || normalize(dog.getBreed().breed).contains(breedQuery))
                && (sizeQuery == null || normalize(dog.getSize().size).equals(sizeQuery))
                && (id == null || dog.getId() == id);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String validateName(String value) {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Dog search query cannot be blank");
        }
        return value;
    }

    private static int validateId(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Dog search ID must be positive");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DogSearchPredicate otherPredicate)) {
            return false;
        }
        return Objects.equals(nameQuery, otherPredicate.nameQuery)
                && Objects.equals(breedQuery, otherPredicate.breedQuery)
                && Objects.equals(sizeQuery, otherPredicate.sizeQuery)
                && Objects.equals(id, otherPredicate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameQuery, breedQuery, sizeQuery, id);
    }
}
