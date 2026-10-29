//@@author xandergoh-unused
// Retired in v1.5: resetdisplay restores records; field-based searches narrow the display.
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Lists all dogs in the address book. */
public class ListDogCommand extends Command {
    public static final String COMMAND_WORD = "list dog";
    public static final String MESSAGE_SUCCESS = "Listed all dogs.";
    public static final String MESSAGE_EMPTY = "There are no dogs to display.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredCustomerList(customer -> false);
        model.updateFilteredDogList(dog -> true);
        if (model.getDogList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
