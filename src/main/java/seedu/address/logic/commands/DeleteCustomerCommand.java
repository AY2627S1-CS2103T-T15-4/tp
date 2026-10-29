package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.Customer;

/** Deletes a customer identified by its persistent ID and unlinks their dogs. */
public class DeleteCustomerCommand extends Command {

    public static final String COMMAND_WORD = "delete customer";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " <id>: Deletes the customer with the specified ID and unlinks their dogs without deleting them.\n"
            + "Example: delete customer 21";
    public static final String MESSAGE_SUCCESS = "Successfully deleted customer: %1$s; id: %2$d; "
            + "Contact: %3$s; Email: %4$s; Backup contact: %5$s";
    public static final String MESSAGE_INVALID_ID = "Error in deleting customer:\nPlease enter a valid customer index";

    private static final Logger logger = LogsCenter.getLogger(DeleteCustomerCommand.class);

    private final int customerId;

    /** Creates a command to delete the customer with the specified ID. */
    public DeleteCustomerCommand(int customerId) {
        checkArgument(customerId >= 1, "Customer ID must be positive.");
        this.customerId = customerId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Customer customerToDelete = model.getCustomerList().stream()
                .filter(customer -> customer.getId() == customerId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("No customer found with ID " + customerId);
                    return new CommandException(MESSAGE_INVALID_ID);
                });
        model.deleteCustomer(customerToDelete);
        logger.info("Deleted customer ID " + customerId);
        return new CommandResult(String.format(MESSAGE_SUCCESS, customerToDelete.getName(),
                customerToDelete.getId(), customerToDelete.getPhone(), customerToDelete.getEmail(),
                customerToDelete.getBackupContact()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteCustomerCommand otherCommand && customerId == otherCommand.customerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("customerId", customerId).toString();
    }
}
