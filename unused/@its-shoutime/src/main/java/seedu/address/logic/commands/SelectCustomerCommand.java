package seedu.address.logic.commands;
//@@author its-shoutime-unused
// Archived because select customer is not part of the final product.

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.Customer;

/** Shows one customer, that customer's dogs, and those dogs' current and future activities. */
public class SelectCustomerCommand extends Command {

    public static final String COMMAND_WORD = "select";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " customer <id>: Shows the customer, their dogs, and those dogs' current and future activities.\n"
            + "Example: " + COMMAND_WORD + " customer 21";
    public static final String MESSAGE_SUCCESS = "Selected customer %1$d: %2$s";
    public static final String MESSAGE_INVALID_ID = "No such customer. Input a valid customer id";

    private static final Logger logger = LogsCenter.getLogger(SelectCustomerCommand.class);

    private final int customerId;

    /** Creates a command that selects the customer with the specified ID. */
    public SelectCustomerCommand(int customerId) {
        assert customerId >= 1 : "Customer ID must be positive";
        this.customerId = customerId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Customer selectedCustomer = model.getCustomerList().stream()
                .filter(customer -> customer.getId() == customerId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("No customer found with ID " + customerId);
                    return new CommandException(MESSAGE_INVALID_ID);
                });

        model.selectCustomer(selectedCustomer.getId());
        logger.info("Selected customer ID " + selectedCustomer.getId());
        return new CommandResult(String.format(MESSAGE_SUCCESS, selectedCustomer.getId(),
                selectedCustomer.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SelectCustomerCommand otherCommand && customerId == otherCommand.customerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("customerId", customerId).toString();
    }
}
