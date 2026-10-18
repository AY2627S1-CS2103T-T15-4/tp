package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Marks an activity as completed. */
public class MarkActivityCommand extends Command {
    public static final String COMMAND_WORD = "mark";
    public static final String MESSAGE_USAGE = "mark: Marks an activity as completed. Parameters: mark ACTIVITY_ID";
    public static final String MESSAGE_SUCCESS = "Activity %d marked as done.";

    private final int activityId;

    /** Creates a command for the specified activity ID. */
    public MarkActivityCommand(int activityId) {
        if (activityId < 0) {
            throw new IllegalArgumentException("Activity ID must be nonnegative.");
        }
        this.activityId = activityId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.markActivity(activityId);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, activityId));
    }
}
