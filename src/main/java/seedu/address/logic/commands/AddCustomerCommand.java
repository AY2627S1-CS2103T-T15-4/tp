package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.Customer;

/** Adds a customer to WatchDog. */
public class AddCustomerCommand extends Command {
    public static final String COMMAND_WORD = "addcustomer";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a customer. "
            + "Parameters: n/NAME p/PHONE e/EMAIL bc/BACKUP_CONTACT\n"
            + "Example: " + COMMAND_WORD + " n/Alice Tan p/91234567 e/alice@example.com bc/98765432";
    public static final String MESSAGE_SUCCESS = "New customer added.";
    public static final String MESSAGE_DUPLICATE_CUSTOMER = "This customer already exists in WatchDog.";

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

        CustomerContactValidator.validate(toAdd, model.getCustomerList(), null);
        try {
            model.addCustomer(toAdd);
        } catch (IllegalStateException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(MESSAGE_SUCCESS);
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
