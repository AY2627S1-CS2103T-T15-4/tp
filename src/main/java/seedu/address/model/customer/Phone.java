// @@author lilosy
package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a customer's phone number in WatchDog.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should contain exactly 8 digits, or start with + followed by 7 to 15 digits "
                    + "including the country code (first digit 1-9); spaces and hyphens are allowed between digits";
    public static final String VALIDATION_REGEX =
            "(?:[0-9](?:[ -]*[0-9]){7}|\\+[1-9](?:[ -]*[0-9]){6,14})";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = normalize(phone);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /** Removes formatting from a valid number and stores Singapore +65 numbers in local form. */
    static String normalize(String phone) {
        String normalized = phone.replace(" ", "").replace("-", "");
        if (normalized.startsWith("+65") && normalized.length() == 11) {
            return normalized.substring(3);
        }
        return normalized;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
