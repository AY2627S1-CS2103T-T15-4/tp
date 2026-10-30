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
            "Backup contact numbers should contain exactly 8 digits, or start with + followed by 7 to 15 digits "
                    + "including the country code (first digit 1-9); spaces and hyphens are allowed between digits";
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
