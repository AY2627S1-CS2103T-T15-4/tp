package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";
    public static final String MESSAGE_CONFIRMATION = "Clear all customers, dogs, activities and enrolments? "
            + "Warning: this action cannot be undone. Enter Y to confirm or N to cancel (Y/N).";
    public static final String MESSAGE_CANCELLED = "Clear cancelled. No data has been deleted.";
    public static final String MESSAGE_INVALID_CONFIRMATION = "Please enter Y or N. " + MESSAGE_CONFIRMATION;


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
