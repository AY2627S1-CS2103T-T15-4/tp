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
        if (model.getDogList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY, CommandResult.DisplayedRecords.ALL);
        }
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.DisplayedRecords.ALL);
    }
}
