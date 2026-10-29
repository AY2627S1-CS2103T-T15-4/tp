package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Format: help [COMMAND]\nExample: help summary\n"
            + "Use help alone to open the help window.";

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    private final String commandHelp;

    public HelpCommand() {
        commandHelp = null;
    }

    /** Creates help to display in the result section instead of opening the help window. */
    public HelpCommand(String commandHelp) {
        requireNonNull(commandHelp);
        checkArgument(!commandHelp.isBlank(), "Command help must not be blank.");
        this.commandHelp = commandHelp;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (commandHelp != null) {
            return new CommandResult(commandHelp);
        }
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
