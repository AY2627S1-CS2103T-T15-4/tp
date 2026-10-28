package seedu.address.model.dog;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a dog's size in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidSize(String)}
 */
public class Size {

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid size. Please enter small, medium, large, or giant.";

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
        checkArgument(isValidSize(sizeInput), MESSAGE_CONSTRAINTS);
        size = sizeInput;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidSize(String test) {
        return test.matches(SIZE_REGEX);
    }


    @Override
    public String toString() {
        return size;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Size otherSize)) {
            return false;
        }
        return size.equals(otherSize.size);
    }

    @Override
    public int hashCode() {
        return size.hashCode();
    }

}
