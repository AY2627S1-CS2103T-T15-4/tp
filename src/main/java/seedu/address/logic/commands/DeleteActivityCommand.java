// @@author lilosy
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;

/** Deletes an activity series identified by its persistent ID and removes its enrolments. */
public class DeleteActivityCommand extends Command {

    public static final String COMMAND_WORD = "deleteactivity";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " ID: Deletes the activity with the specified ID and removes its dog enrolments.\n"
            + "Example: deleteactivity 21";
    public static final String MESSAGE_SUCCESS = "Successfully deleted activity %1$d: %2$s";
    public static final String MESSAGE_INVALID_ID = "Activity ID %d does not exist.";
    public static final String MESSAGE_INVALID_ACTIVITY_LIST = "Cannot delete activity: the activity list is invalid.";

    private static final Logger logger = LogsCenter.getLogger(DeleteActivityCommand.class);

    private final int activityId;

    /** Creates a command to delete the activity with the specified ID. */
    public DeleteActivityCommand(int activityId) {
        if (activityId <= 0) {
            throw new IllegalArgumentException("Activity ID must be positive.");
        }
        this.activityId = activityId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        ObservableList<Activity> activities = model.getActivityList();
        if (activities == null || activities.stream().anyMatch(Objects::isNull)) {
            logger.warning("Cannot delete activity ID " + activityId + ": activity list is null or contains null.");
            throw new CommandException(MESSAGE_INVALID_ACTIVITY_LIST);
        }

        Activity activityToDelete = activities.stream()
                .filter(activity -> activity.getId() == activityId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("No activity found with ID " + activityId);
                    return new CommandException(String.format(MESSAGE_INVALID_ID, activityId));
                });
        try {
            model.deleteActivity(activityToDelete);
        } catch (IllegalArgumentException e) {
            logger.warning("Could not delete activity ID " + activityId + ": " + e.getMessage());
            throw new CommandException(e.getMessage(), e);
        }
        logger.info("Deleted activity ID " + activityId);
        return new CommandResult(String.format(MESSAGE_SUCCESS, activityToDelete.getId(), activityToDelete.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteActivityCommand otherCommand && activityId == otherCommand.activityId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("activityId", activityId).toString();
    }
}
