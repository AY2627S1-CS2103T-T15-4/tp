// Retired in v1.5 in favour of resetdisplay and field-based searches.
// Unannotated portions retain their AB3 provenance; tags preserve teammate contributions.
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_CUSTOMERS;

import seedu.address.model.Model;

/**
 * Lists all customers in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    //@@author its-shoutime-unused
    public static final String MESSAGE_SUCCESS = "Listed all customers.";
    public static final String MESSAGE_EMPTY = "There are no customers to display.";
    //@@author xandergoh-unused
    public static final String MESSAGE_MISSING_CATEGORY = "Please input which category you’d like to view. "
            + "Use the following format: list <category> (e.g list dog, list customer)";

    public static final String MESSAGE_USAGE = "list customer: Lists all customers.\n"
            + "list dog: Lists all dogs.";


    //@@author its-shoutime-unused
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredDogList(dog -> false);
        model.updateFilteredCustomerList(PREDICATE_SHOW_ALL_CUSTOMERS);
        if (model.getFilteredCustomerList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ListCommand;
    }
}
