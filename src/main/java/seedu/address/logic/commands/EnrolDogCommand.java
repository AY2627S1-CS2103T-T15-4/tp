package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.EnrolException;

/** Enrols a batch of dogs in an activity using persistent IDs. */
public class EnrolDogCommand extends Command {
    public static final String COMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = "enrol: Enrols one or more dogs to an activity.\n"
            + "Parameters: a/ACTIVITY_ID d/DOG_ID1, DOG_ID2...\n"
            + "IDs must be integers from 0 to 2147483647; dog IDs must not repeat.\n"
            //"The entire batch is rejected if any record is missing or any dog already has t.\n"
            + "Example: enrol a/1 d/2,3";
    public static final String MESSAGE_SUCCESS = "Enrolled dog IDs %s to activity ID %d.";

    private final int activityId;
    private final List<Integer> dogIds;

    /**
     * Creates a link request containing distinct nonnegative dog IDs and a nonnegative activity ID.
     */
    public EnrolDogCommand(int activityId, List<Integer> dogIds) {
        this.dogIds = List.copyOf(dogIds);
        checkArgument(activityId >= 0 && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 0)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid enrol IDs.");
        this.activityId = activityId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.enrolDogs(activityId, dogIds);
        } catch (EnrolException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, activityId));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EnrolDogCommand otherCommand
                && activityId == otherCommand.activityId && dogIds.equals(otherCommand.dogIds);
    }
}
