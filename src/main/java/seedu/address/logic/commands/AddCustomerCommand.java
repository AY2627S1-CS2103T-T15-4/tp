package seedu.address.logic.commands;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.Customer;
import seedu.address.model.person.Person;

import static java.util.Objects.requireNonNull;

public class AddCustomerCommand extends Command{
    public static final String COMMAND_WORD = "add customer";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds customer to contacts";
    public static final String MESSAGE_SUCCESS = "New customer added: %1$s";

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    private final Customer toAdd;

    public AddCustomerCommand(Customer customer) {
        requireNonNull(customer);
        toAdd = customer;
    }

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult("Hello from remark");
    }

}
