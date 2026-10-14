package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.Customer;

/** Adds a customer to the address book. */
public class AddCustomerCommand extends Command {
    public static final String COMMAND_WORD = "addcustomer";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds customer to contacts";
    public static final String MESSAGE_SUCCESS = "New customer added: %1$s";
    public static final String MESSAGE_DUPLICATE_CUSTOMER = "This customer already exists in the address book.";

    /**
     * Creates an AddCustomerCommand to add the specified {@code Customer}.
     */
    private final Customer toAdd;

    /**
     * Creates an AddCustomerCommand for the specified {@code Customer}.
     */
    public AddCustomerCommand(Customer customer) {
        requireNonNull(customer);
        toAdd = customer;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasCustomer(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_CUSTOMER);
        }

        model.addCustomer(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddCustomerCommand otherAddCustomerCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCustomerCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
