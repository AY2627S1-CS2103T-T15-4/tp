package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CUSTOMERS;

import seedu.address.model.Model;

/**
 * Lists all customers in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all customers.";
    public static final String MESSAGE_EMPTY = "There are no customers to display.";
    public static final String MESSAGE_MISSING_CATEGORY = "Please input which category you’d like to view. "
            + "Use the following format: list <category> (e.g list dog, list customer)";

    public static final String MESSAGE_USAGE = "list customer: Lists all customers.\n"
            + "list dog: Lists all dogs.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredCustomerList(PREDICATE_SHOW_ALL_CUSTOMERS);
        if (model.getFilteredCustomerList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY, CommandResult.DisplayedRecords.CUSTOMERS);
        }
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.DisplayedRecords.CUSTOMERS);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListCommand;
    }
}
