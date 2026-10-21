package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/** Filters dogs or customers that are not linked to the other record type. */
public class FilterUnlinkedCommand extends Command {
    public static final String COMMAND_WORD = "filterun";
    public static final String MESSAGE_SUCCESS_DOG = "Successfully filtered dogs by unlinked";
    public static final String MESSAGE_SUCCESS_CUSTOMER = "Successfully filtered customers by unlinked";

    private final boolean filterDogs;

    /** Creates an unlinked-record filter. */
    public FilterUnlinkedCommand(boolean filterDogs) {
        this.filterDogs = filterDogs;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (filterDogs) {
            model.updateFilteredDogList(dog -> dog.getOwnerId().isEmpty());
            model.updateFilteredCustomerList(customer -> false);
            return new CommandResult(MESSAGE_SUCCESS_DOG);
        }
        model.updateFilteredCustomerList(customer -> model.getDogList().stream()
                .noneMatch(dog -> dog.getOwnerId().orElse(-1) == customer.getId()));
        model.updateFilteredDogList(dog -> false);
        return new CommandResult(MESSAGE_SUCCESS_CUSTOMER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FilterUnlinkedCommand otherCommand
                && filterDogs == otherCommand.filterDogs;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(filterDogs);
    }
}
