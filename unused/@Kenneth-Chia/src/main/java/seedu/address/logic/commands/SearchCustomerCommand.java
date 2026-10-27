package seedu.address.logic.commands;
//@@author Kenneth-Chia-unused
// Original category-search implementation, superseded by searchc in PR #150;
// retained for authorship and reference.

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.customer.CustomerSearchPredicate;

/** Shows matching customer cards without changing stored records. */
public class SearchCustomerCommand extends Command {
    public static final String COMMAND_WORD = "search";
    public static final String MESSAGE_USAGE = "search customer: Searches customers by name substring"
            + " or persistent ID.\n"
            + "Parameters: customer n/NAME_QUERY OR customer id/ID (use exactly one field)\n"
            + "ID must be an integer from 0 to 2147483647.\n"
            + "Examples: search customer n/alex | search customer id/0";
    public static final String MESSAGE_RESULT = "%d customers found.";

    private final CustomerSearchPredicate predicate;

    /** Creates a customer search with the given matching rule. */
    public SearchCustomerCommand(CustomerSearchPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredDogList(dog -> false);
        model.updateFilteredCustomerList(predicate);
        return new CommandResult(String.format(MESSAGE_RESULT, model.getFilteredCustomerList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SearchCustomerCommand otherCommand
                && predicate.equals(otherCommand.predicate);
    }
}
