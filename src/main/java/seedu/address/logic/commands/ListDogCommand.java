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
            return new CommandResult(MESSAGE_EMPTY);
        }
        StringBuilder result = new StringBuilder(MESSAGE_SUCCESS);
        model.getDogList().forEach(dog -> result.append("\n").append(dog));
        return new CommandResult(result.toString());
    }
}
