package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Creates an activity schedule without booking any dogs. */
public class AddActivityCommand extends Command {
    public static final String COMMAND_WORD = "addactivity";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an activity with zero dogs. Parameters: n/NAME date/YYYY-MM-DD start/HH:mm end/HH:mm "
            + "r/once|daily|weekly\nExample: " + COMMAND_WORD
            + " n/Training date/2026-10-20 start/10:00 end/11:00 r/weekly";
    public static final String MESSAGE_SUCCESS = "New activity added: ID %d; %s; First date: %s; %s-%s; %s";

    // A validated details snapshot; its ID is unused. Only the model assigns IDs on execution.
    private final Activity details;

    /** Validates details without consuming a persistent ID. */
    public AddActivityCommand(ActivityName name, LocalDate date, LocalTime start, LocalTime end,
            Recurrence recurrence) {
        details = new Activity(0, name, date, start, end, recurrence);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Activity added;
        try {
            added = model.addActivity(details.getName(), details.getStartDate(), details.getStartTime(),
                    details.getEndTime(), details.getRecurrence());
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, added.getId(), added.getName(), added.getStartDate(),
                added.getStartTime(), added.getEndTime(), added.getRecurrence()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AddActivityCommand command && details.equals(command.details);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("details", details).toString();
    }
}
