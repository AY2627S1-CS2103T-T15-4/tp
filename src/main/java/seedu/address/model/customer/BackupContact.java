//@@author lilosy
package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;


/**
 * Represents a customer's backup contact number in WatchDog.
 * Guarantees: immutable; is valid as declared in {@link #isValidBackupContact(String)}
 */
public class BackupContact {
    public static final String MESSAGE_CONSTRAINTS =
            "Enter a backup contact number in either format:\n"
                    + "Local: exactly 8 digits without a country code, e.g. 91234567.\n"
                    + "International: + followed by 7 to 15 digits including the country code, "
                    + "e.g. +6591234567 (10 digits after +).\n"
                    + "Singapore numbers with +65 must have exactly 8 digits after +65.\n"
                    + "The + sign does not count as a digit. The first digit after + must be 1-9. "
                    + "Spaces and hyphens are allowed between digits.";
    public static final String VALIDATION_REGEX = Phone.VALIDATION_REGEX;
    public final String value;

    /**
     * Constructs a {@code BackupContact}.
     *
     * @param backupContact A valid phone number.
     */
    public BackupContact(String backupContact) {
        requireNonNull(backupContact);
        checkArgument(isValidBackupContact(backupContact), MESSAGE_CONSTRAINTS);
        value = Phone.normalize(backupContact);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidBackupContact(String test) {
        return Phone.isValidPhone(test);
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
        if (!(other instanceof BackupContact otherBackupContact)) {
            return false;
        }

        return value.equals(otherBackupContact.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
