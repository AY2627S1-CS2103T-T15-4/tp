// @@author lilosy
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CUSTOMERS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_DOGS;

import java.time.Clock;
import java.time.LocalDate;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;

/** Restores all customers and dogs and shows activities occurring today or later. */
public class ResetDisplayCommand extends Command {
    public static final String COMMAND_WORD = "reset";
    public static final String MESSAGE_USAGE = "reset: Shows all customers, dogs, and upcoming activities.";
    public static final String MESSAGE_SUCCESS = "Showing all customers, dogs, and activities from %s onward.";

    private static final Logger logger = LogsCenter.getLogger(ResetDisplayCommand.class);

    private final Clock clock;

    public ResetDisplayCommand() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a command with a specified clock so the current date can be controlled in tests. */
    public ResetDisplayCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        model.updateFilteredCustomerList(PREDICATE_SHOW_ALL_CUSTOMERS);
        model.updateFilteredDogList(PREDICATE_SHOW_ALL_DOGS);
        model.showActivitiesOnOrAfter(today);
        logger.info(() -> String.format("Reset display from %s: customers=%d dogs=%d activities=%d",
                today, model.getFilteredCustomerList().size(), model.getFilteredDogList().size(),
                model.getFilteredActivityList().size()));
        return new CommandResult(String.format(MESSAGE_SUCCESS, today));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ResetDisplayCommand;
    }
}
