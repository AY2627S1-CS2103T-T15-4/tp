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
            + "Parameters (in any order): a/ACTIVITY_ID date/YYYY-MM-DD d/DOG_ID [DOG_ID...]\n"
            + "Activity and dog IDs must be positive; dog IDs must not repeat.\n"
            + "Example: enrol a/1 date/2028-02-29 d/2 3";
    public static final String MESSAGE_FORMAT_GUIDANCE = "\nFormat: enrol a/ACTIVITY_ID "
            + "date/YYYY-MM-DD d/DOG_ID [DOG_ID...]"
            + "\nExample: enrol a/1 date/2028-02-29 d/2 3";

    public static final String MESSAGE_SUCCESS = "Enrolled dog IDs %s to activity ID %d on %s.";

    private final int activityId;
    private final LocalDate date;
    private final List<Integer> dogIds;

    /**
     * Creates an enrol request containing a positive activity ID and distinct positive dog IDs.
     *
     * @throws IllegalArgumentException If the IDs, date, or dog ID collection are invalid.
     * @throws NullPointerException If {@code dogIds} is null.
     */
    public EnrolDogCommand(int activityId, LocalDate date, List<Integer> dogIds) {
        requireNonNull(dogIds);
        checkArgument(activityId >= 1 && date != null && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id != null && id >= 1)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid enrol IDs.");
        this.activityId = activityId;
        this.date = date;
        this.dogIds = List.copyOf(dogIds);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.enrolDogs(activityId, date, dogIds);
        } catch (EnrolException e) {
            throw new CommandException(e.getMessage() + MESSAGE_FORMAT_GUIDANCE, e);
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
