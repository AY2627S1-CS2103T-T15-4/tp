//@@author its-shoutime
package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Restores the state before the most recent successful undoable command. */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_NO_COMMAND = "There is no command to undo.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Undid command: %s";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(MESSAGE_NO_COMMAND);
    }
}
