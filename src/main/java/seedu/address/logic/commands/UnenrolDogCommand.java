package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.EnrolException;

/** Removes a batch of dogs from an activity using persistent IDs. */
public class UnenrolDogCommand extends Command {
    public static final String COMMAND_WORD = "unenrol";
    public static final String MESSAGE_USAGE = "unenrol: Removes one or more dogs from an activity.\n"
            + "Parameters (in any order): a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]\n"
            + "Activity and dog IDs must be positive; dog IDs must not repeat.\n"
            + "Example: unenrol a/1 date/2028-02-29 d/2 3";
    public static final String MESSAGE_FORMAT_GUIDANCE = "\nFormat: unenrol a/ACTIVITY_ID "
            + "date/YYYY-MM-DD d/DOG_ID [DOG_ID...]"
            + "\nExample: unenrol a/1 date/2028-02-29 d/2 3";
    public static final String MESSAGE_SUCCESS = "Unenrolled dog IDs %s from activity ID %d on %s.";

    private final int activityId;
    private final LocalDate date;
    private final List<Integer> dogIds;

    /** Creates a command containing a positive activity ID and distinct positive dog IDs. */
    public UnenrolDogCommand(int activityId, LocalDate date, List<Integer> dogIds) {
        checkArgument(activityId >= 1 && date != null && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 1)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid unenrol IDs.");
        this.activityId = activityId;
        this.date = date;
        this.dogIds = List.copyOf(dogIds);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.unenrolDogs(activityId, date, dogIds);
        } catch (EnrolException e) {
            throw new CommandException(e.getMessage() + MESSAGE_FORMAT_GUIDANCE, e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, activityId, date));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnenrolDogCommand otherCommand
                && activityId == otherCommand.activityId && date.equals(otherCommand.date)
                && dogIds.equals(otherCommand.dogIds);
    }
}
