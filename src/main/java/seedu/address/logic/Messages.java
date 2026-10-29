package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /** Formats the {@code dog} for display to the user. */
    public static String format(Dog dog) {
        final StringBuilder builder = new StringBuilder();
        builder.append(dog.getId())
                .append("; Name: ")
                .append(dog.getName())
                .append("; Breed: ")
                .append(dog.getBreed())
                .append("; Size: ")
                .append(dog.getSize());
        return builder.toString();
    }

    /** Formats the {@code customer} for display to the user. */
    public static String format(Customer customer) {
        final StringBuilder builder = new StringBuilder();
        builder.append(customer.getId())
                .append("; Name: ")
                .append(customer.getName())
                .append("; Phone: ")
                .append(customer.getPhone())
                .append("; Email: ")
                .append(customer.getEmail())
                .append("; Backup Contact: ")
                .append(customer.getBackupContact());
        return builder.toString();
    }
}
