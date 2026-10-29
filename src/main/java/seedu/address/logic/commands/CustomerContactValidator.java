package seedu.address.logic.commands;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.customer.Customer;

/**
 * Checks that customer commands do not reuse another customer's primary contact details.
 */
final class CustomerContactValidator {
    static final String MESSAGE_DUPLICATE_PHONE = "This phone number is already used by another customer.";
    static final String MESSAGE_DUPLICATE_EMAIL = "This email address is already used by another customer.";

    private CustomerContactValidator() {
    }

    /**
     * Rejects duplicate primary contacts, excluding the customer being edited when an ID is supplied.
     * Phone values are already normalized; emails are compared without regard to letter case.
     */
    static void validate(Customer candidate, List<Customer> customers, Integer excludedCustomerId)
            throws CommandException {
        for (Customer customer : customers) {
            if (excludedCustomerId != null && customer.getId() == excludedCustomerId) {
                continue;
            }
            if (customer.getPhone().equals(candidate.getPhone())) {
                throw new CommandException(MESSAGE_DUPLICATE_PHONE);
            }
            if (customer.getEmail().value.equalsIgnoreCase(candidate.getEmail().value)) {
                throw new CommandException(MESSAGE_DUPLICATE_EMAIL);
            }
        }
    }
}
