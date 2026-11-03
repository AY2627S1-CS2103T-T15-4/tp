//@@author lilosy
package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a customer's email in WatchDog.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part must be nonempty and contain only ASCII letters, digits, "
            + "and these special characters, "
            + "excluding the parentheses, (" + Email.SPECIAL_CHARACTERS
            + "). Dots must not appear at the start or end, or consecutively. "
            + "Other supported special characters may appear anywhere, including consecutively.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods. A single label is allowed; a period is not required.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with an ASCII letter or digit\n"
            + "    - contain only ASCII letters, digits, and single hyphens within each label; "
            + "consecutive hyphens are not supported.";
    public static final String VALIDATION_REGEX = Email.LOCAL_PART_REGEX + "@" + Email.DOMAIN_REGEX;

    // Keep the hyphen last so it is treated literally in the regex character group.
    private static final String SPECIAL_CHARACTERS = "+_.'-";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    // A nonempty sequence of dot-separated atoms; only dots have placement restrictions.
    private static final String LOCAL_ATOM_REGEX = "[a-zA-Z0-9+_'-]+";
    private static final String LOCAL_PART_REGEX = "^" + LOCAL_ATOM_REGEX + "(?:\\." + LOCAL_ATOM_REGEX + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(?=.{2,}$)" + DOMAIN_PART_REGEX;
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(VALIDATION_REGEX);
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
