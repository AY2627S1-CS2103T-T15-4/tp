package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears all customers, dogs, activities and enrolments from the address book.
 * The logic manager obtains confirmation before executing this command and discards undo/redo history afterwards.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "WatchDog data has been cleared!";
    public static final String MESSAGE_CONFIRMATION = "Clear all customers, dogs, activities and enrolments? "
            + "Warning: this action cannot be undone. Enter Y to confirm or N to cancel (Y/N).";
    public static final String MESSAGE_CANCELLED = "Clear cancelled. No data has been deleted.";
    public static final String MESSAGE_INVALID_CONFIRMATION = "Please enter Y or N. " + MESSAGE_CONFIRMATION;


    /**
     * Replaces the address book with an empty one. The caller is responsible for obtaining confirmation.
     *
     * @param model The model whose address book is cleared.
     * @return The success message after clearing the address book.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
