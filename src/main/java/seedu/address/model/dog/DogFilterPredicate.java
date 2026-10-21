package seedu.address.model.dog;

import java.util.Locale;
import java.util.function.Predicate;

/** Filters dogs by name, breed, size, and ID. */
public class DogFilterPredicate implements Predicate<Dog> {
    private final String nameQuery;
    private final String breedQuery;
    private final String sizeQuery;
    private final Integer id;

    /** Creates a predicate from any combination of supported dog fields. */
    public DogFilterPredicate(String nameQuery, String breedQuery, String sizeQuery, Integer id) {
        this.nameQuery = normalize(nameQuery);
        this.breedQuery = normalize(breedQuery);
        this.sizeQuery = normalize(sizeQuery);
        this.id = id;
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
}
