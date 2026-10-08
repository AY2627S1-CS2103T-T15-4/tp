package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Size {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should only contain alphanumeric characters and spaces, and should not be blank";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String SIZE_REGEX = "(?i)^(small|medium|large|giant)$";

    public final String size;

    /**
     * Constructs a {@code Size}.
     *
     * @param sizeInput A valid size.
     */
    public Size(String sizeInput) {
        requireNonNull(sizeInput);
        checkArgument(isValidName(sizeInput), MESSAGE_CONSTRAINTS);
        size = sizeInput;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(SIZE_REGEX);
    }


    @Override
    public String toString() {
        return size;
    }

        @Override
    public int hashCode() {
        return size.hashCode();
    }

}
