package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CUSTOMERS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_DOGS;

import seedu.address.model.Model;

/** Restores the display of all customers, dogs, and activities. */
public class ResetDisplayCommand extends Command {
    public static final String COMMAND_WORD = "resetdisplay";
    public static final String MESSAGE_USAGE = "resetdisplay: Shows all customers, dogs, and activities.";
    public static final String MESSAGE_SUCCESS = "Showing all customers, dogs, and activities.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredCustomerList(PREDICATE_SHOW_ALL_CUSTOMERS);
        model.updateFilteredDogList(PREDICATE_SHOW_ALL_DOGS);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ResetDisplayCommand;
    }
}
