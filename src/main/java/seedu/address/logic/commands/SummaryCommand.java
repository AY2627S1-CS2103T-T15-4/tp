package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;

/** Shows overall record counts and today's schedules and enrolled dogs without changing the view. */
public class SummaryCommand extends Command {
    public static final String COMMAND_WORD = "summary";
    public static final String MESSAGE_USAGE = "summary: Shows overall counts and today's activities and enrolled dogs."
            + " Does not accept parameters.";
    public static final String MESSAGE_SUCCESS = "Overall: %d dogs | %d customers\n"
            + "Activity schedules: %d total | %d from today onwards\n"
            + "Today (%s): %d activities | %d distinct dogs enrolled";

    private static final Logger logger = LogsCenter.getLogger(SummaryCommand.class);

    private final Clock clock;

    public SummaryCommand() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a command whose local date is read from the supplied clock at execution. */
    public SummaryCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        List<Activity> activities = model.getActivityList();
        long upcomingSchedules = activities.stream()
                .filter(activity -> activity.nextOccurrenceOnOrAfter(today).isPresent()).count();
        long todaysActivities = activities.stream().filter(activity -> activity.occursOn(today)).count();
        logger.fine("Summary for " + today + ": " + activities.size() + " schedules, "
                + upcomingSchedules + " upcoming, " + todaysActivities + " occurring today.");
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getDogList().size(),
                model.getCustomerList().size(), activities.size(), upcomingSchedules, today,
                todaysActivities, model.getDogsEnrolledOn(today).size()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SummaryCommand;
    }

    @Override
    public int hashCode() {
        return SummaryCommand.class.hashCode();
    }
}
