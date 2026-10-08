package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Breed {

    public static final String MESSAGE_CONSTRAINTS =
            "Breeds should only contain alphanumeric characters and spaces, and should not be blank";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String breed;

    /**
     * Constructs a {@code breedInput}.
     *
     * @param breedInput A valid name.
     */
    public Breed(String breedInput) {
        requireNonNull(breedInput);
        checkArgument(isValidName(breedInput), MESSAGE_CONSTRAINTS);
        breed = breedInput;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return breed;
    }

    @Override
    public int hashCode() {
        return breed.hashCode();
    }

}
