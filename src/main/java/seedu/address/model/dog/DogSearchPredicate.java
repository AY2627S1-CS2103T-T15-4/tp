package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;

/** Matches a dog's name substring or its exact persistent ID. */
public class DogSearchPredicate implements Predicate<Dog> {
    private final String nameQuery;
    private final Integer id;

    /** Creates a case-insensitive name substring search. */
    public DogSearchPredicate(String nameQuery) {
        this.nameQuery = requireNonNull(nameQuery).strip();
        checkArgument(!this.nameQuery.isEmpty(), "Name query cannot be blank");
        this.id = null;
    }

    /** Creates a search for a nonnegative persistent ID, not a displayed list index. */
    public DogSearchPredicate(int id) {
        checkArgument(id >= 0, "Dog ID must be nonnegative");
        this.id = id;
        this.nameQuery = null;
    }

    @Override
    public boolean test(Dog dog) {
        requireNonNull(dog);
        return id == null
                ? StringUtil.containsSubstringIgnoreCase(dog.getName().fullName, nameQuery)
                : dog.getId() == id;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DogSearchPredicate otherPredicate
                && Objects.equals(nameQuery, otherPredicate.nameQuery)
                && Objects.equals(id, otherPredicate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameQuery, id);
    }
}
