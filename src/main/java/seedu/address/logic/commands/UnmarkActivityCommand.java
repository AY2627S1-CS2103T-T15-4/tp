package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Marks an activity as incomplete. */
public class UnmarkActivityCommand extends Command {
    public static final String COMMAND_WORD = "unmark";
    public static final String MESSAGE_USAGE = "unmark: Marks an activity as incomplete. "
            + "Parameters: unmark ACTIVITY_ID";
    public static final String MESSAGE_SUCCESS = "Activity %d marked as pending.";

    private final int activityId;

    /** Creates a command for the specified activity ID. */
    public UnmarkActivityCommand(int activityId) {
        if (activityId < 0) {
            throw new IllegalArgumentException("Activity ID must be nonnegative.");
        }
        this.activityId = activityId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.unmarkActivity(activityId);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, activityId));
    }
}
