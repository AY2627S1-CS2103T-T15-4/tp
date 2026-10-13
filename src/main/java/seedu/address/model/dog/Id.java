package seedu.address.model.dog;

import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a dog's persistent ID.
 * Guarantees: immutable and nonnegative.
 */
public class Id {

    public static final String MESSAGE_CONSTRAINTS = "ID must be a nonnegative integer.";

    private final int value;

    /**
     * Constructs an {@code Id}.
     *
     * @param value A nonnegative ID.
     */
    public Id(int value) {
        checkArgument(isValidId(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValidId(int value) {
        return value >= 0;
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Id otherId)) {
            return false;
        }

        return value == otherId.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

}
