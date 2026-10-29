package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.WatchDog;

/**
 * Clears all customers, dogs, activities and enrolments from WatchDog.
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
     * Replaces WatchDog with an empty one. The caller is responsible for obtaining confirmation.
     *
     * @param model The model whose WatchDog is cleared.
     * @return The success message after clearing WatchDog.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setWatchDog(new WatchDog());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
