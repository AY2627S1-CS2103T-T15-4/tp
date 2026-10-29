package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class CustomerContactValidatorTest {

    @Test
    public void validate_nullCandidate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CustomerContactValidator.validate(null, List.of(), null));
    }

    @Test
    public void validate_nullCustomerList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CustomerContactValidator.validate(customer(), null, null));
    }

    @Test
    public void validate_nullCustomerEntry_throwsNullPointerException() {
        List<Customer> customers = Arrays.asList((Customer) null);

        assertThrows(NullPointerException.class,
                () -> CustomerContactValidator.validate(customer(), customers, null));
    }

    private static Customer customer() {
        return new Customer(new Name("Alice Tan"), new Phone("91234567"), new Email("alice@example.com"),
                new BackupContact("98765432"));
    }
}
