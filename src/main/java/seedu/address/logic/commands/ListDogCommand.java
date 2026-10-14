package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Lists all dogs in the address book. */
public class ListDogCommand extends Command {
    public static final String COMMAND_WORD = "list dog";
    public static final String MESSAGE_SUCCESS = "Listed all dogs.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
