package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.customer.Customer;
import seedu.address.model.person.Person;

import static java.util.Objects.requireNonNull;

public class AddCustomerCommand extends Command{
    public static final String COMMAND_WORD = "addcustomer";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds customer to contacts";
    public static final String MESSAGE_SUCCESS = "New customer added: %1$s";

    /**
     * Creates an AddCommand to add the specified {@code Person}
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
    public CommandResult execute(Model model) {
        return new CommandResult("Hello from AddCustomer");
    }

}
