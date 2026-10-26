package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Reapplies the command most recently reversed by undo. */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_NO_COMMAND = "There is no command to redo.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Redid command: %s";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(MESSAGE_NO_COMMAND);
    }
}
