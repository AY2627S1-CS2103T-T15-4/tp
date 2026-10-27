package seedu.address.logic.commands;
//@@author Kenneth-Chia-unused
// Original category-search implementation, superseded by searchd in PR #150;
// retained for authorship and reference.

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.dog.DogSearchPredicate;

/** Shows matching dog cards without changing stored records. */
public class SearchDogCommand extends Command {
    public static final String COMMAND_WORD = "search";
    public static final String MESSAGE_USAGE = "search dog: Searches dogs by name substring or persistent ID.\n"
            + "Parameters: dog n/NAME_QUERY OR dog id/ID (use exactly one field)\n"
            + "ID must be an integer from 0 to 2147483647.\n"
            + "Examples: search dog n/bud | search dog id/0";
    public static final String MESSAGE_RESULT = "%d dogs found.";

    private final DogSearchPredicate predicate;

    /** Creates a dog search with the given matching rule. */
    public SearchDogCommand(DogSearchPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredCustomerList(customer -> false);
        model.updateFilteredDogList(predicate);
        return new CommandResult(String.format(MESSAGE_RESULT, model.getFilteredDogList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SearchDogCommand otherCommand
                && predicate.equals(otherCommand.predicate);
    }
}
