package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.EnrolException;

/** Removes a batch of dogs from an activity using persistent IDs. */
public class UnenrolDogCommand extends Command {
    public static final String COMMAND_WORD = "unenrol";
    public static final String MESSAGE_USAGE = "unenrol: Removes one or more dogs from an activity.\n"
            + "Parameters: a/ACTIVITY_ID d/DOG_ID1, DOG_ID2...\n"
            + "Example: unenrol a/1 d/2,3";
    public static final String MESSAGE_SUCCESS = "Unenrolled dog IDs %s from activity ID %d.";

    private final int activityId;
    private final List<Integer> dogIds;

    /** Creates a command containing distinct nonnegative dog and activity IDs. */
    public UnenrolDogCommand(int activityId, List<Integer> dogIds) {
        checkArgument(activityId >= 0 && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 0)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid unenrol IDs.");
        this.activityId = activityId;
        this.dogIds = List.copyOf(dogIds);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.unenrolDogs(activityId, dogIds);
        } catch (EnrolException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, activityId));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnenrolDogCommand otherCommand
                && activityId == otherCommand.activityId && dogIds.equals(otherCommand.dogIds);
    }
}
