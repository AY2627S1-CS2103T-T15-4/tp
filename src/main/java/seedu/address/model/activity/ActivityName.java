package seedu.address.model.activity;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * An immutable, nonblank activity name. Punctuation and internal spaces are permitted.
 */
public final class ActivityName {
    public static final String MESSAGE_CONSTRAINTS = "Activity names must not be blank.";

    public final String fullName;

    /**
     * Creates a name, preserving the supplied text. Parsing is responsible for trimming input.
     */
    public ActivityName(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns whether the supplied nonnull text contains a non-whitespace character.
     */
    public static boolean isValidName(String name) {
        requireNonNull(name);
        return !name.isBlank();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ActivityName otherName && fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public String toString() {
        return fullName;
    }
}
