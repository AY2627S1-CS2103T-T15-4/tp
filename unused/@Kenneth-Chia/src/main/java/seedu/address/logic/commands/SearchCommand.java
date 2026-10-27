//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.model.dog.DogSearchPredicate;

/** Shows matching customers and dogs together without changing stored records. */
public class SearchCommand extends Command {
    public static final String COMMAND_WORD = "search";
    public static final String MESSAGE_USAGE = "search: Searches customers and dogs by name substring"
            + " or persistent ID.\n"
            + "Parameters: n/NAME_QUERY OR id/ID (use exactly one field)\n"
            + "ID must be an integer from 0 to 2147483647.\n"
            + "Examples: search n/alex | search id/0\n"
            + "Use search dog or search customer to search only that category.";
    public static final String MESSAGE_RESULT = "%d customers and %d dogs found.";

    private final CustomerSearchPredicate customerPredicate;
    private final DogSearchPredicate dogPredicate;

    /** Creates a case-insensitive substring search across both categories. */
    public SearchCommand(String nameQuery) {
        requireNonNull(nameQuery);
        if (nameQuery.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be blank");
        }
        customerPredicate = new CustomerSearchPredicate(nameQuery);
        dogPredicate = new DogSearchPredicate(nameQuery);
    }

    /** Creates an exact ID search in both independent ID namespaces. */
    public SearchCommand(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Search ID cannot be negative");
        }
        customerPredicate = new CustomerSearchPredicate(id);
        dogPredicate = new DogSearchPredicate(id);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredCustomerList(customerPredicate);
        model.updateFilteredDogList(dogPredicate);
        return new CommandResult(String.format(MESSAGE_RESULT,
                model.getFilteredCustomerList().size(), model.getFilteredDogList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SearchCommand otherCommand
                && customerPredicate.equals(otherCommand.customerPredicate);
    }
}
