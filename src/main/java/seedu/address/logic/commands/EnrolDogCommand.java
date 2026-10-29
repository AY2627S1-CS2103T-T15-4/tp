package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.EnrolException;

/** Enrols a batch of dogs in an activity using persistent IDs. */
public class EnrolDogCommand extends Command {
    public static final String COMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = "enrol: Enrols one or more dogs to an activity.\n"
            + "Parameters: a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID1, DOG_ID2...\n"
            + "Activity IDs range from 1 to 2147483647; dog IDs from 0 to 2147483647 and must not repeat.\n"
            //"The entire batch is rejected if any record is missing or any dog already has t.\n"
            + "Example: enrol a/1 date/2026-10-27 d/2,3";
    public static final String MESSAGE_SUCCESS = "Enrolled dog IDs %s to activity ID %d on %s.";

    private final int activityId;
    private final LocalDate date;
    private final List<Integer> dogIds;

    /**
     * Creates a link request containing distinct nonnegative dog IDs and a positive activity ID.
     */
    public EnrolDogCommand(int activityId, LocalDate date, List<Integer> dogIds) {
        this.dogIds = List.copyOf(dogIds);
        checkArgument(activityId > 0 && date != null && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 0)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid enrol IDs.");
        this.activityId = activityId;
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.enrolDogs(activityId, date, dogIds);
        } catch (EnrolException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, activityId, date));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EnrolDogCommand otherCommand
                && activityId == otherCommand.activityId && date.equals(otherCommand.date)
                && dogIds.equals(otherCommand.dogIds);
    }
}
