package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.customer.exceptions.CustomerNotFoundException;
import seedu.address.model.customer.exceptions.DuplicateCustomerException;

public class UniqueCustomerListTest {

    private static final Customer ALICE = customer(1, "Alice");
    private static final Customer ALICE_UPDATED = customer(1, "Alice Updated");
    private static final Customer BOB = customer(2, "Bob");

    private final UniqueCustomerList customers = new UniqueCustomerList();

    @Test
    public void addAndContains_useCustomerIdentity() {
        customers.add(ALICE);

        assertTrue(customers.contains(ALICE_UPDATED));
        assertThrows(DuplicateCustomerException.class, () -> customers.add(ALICE_UPDATED));
    }

    @Test
    public void setCustomer_handlesReplacementAndFailures() {
        assertThrows(CustomerNotFoundException.class, () -> customers.setCustomer(ALICE, BOB));

        customers.add(ALICE);
        customers.add(BOB);
        assertThrows(DuplicateCustomerException.class, () -> customers.setCustomer(ALICE, BOB));

        customers.setCustomer(ALICE, ALICE_UPDATED);
        assertEquals(List.of(ALICE_UPDATED, BOB), customers.asUnmodifiableObservableList());
    }

    @Test
    public void removeAndSetCustomers_handleFailuresAndReplacement() {
        assertThrows(CustomerNotFoundException.class, () -> customers.remove(ALICE));
        assertThrows(DuplicateCustomerException.class, () -> customers.setCustomers(List.of(ALICE, ALICE_UPDATED)));

        customers.setCustomers(List.of(BOB));
        assertFalse(customers.contains(ALICE));
        assertTrue(customers.contains(BOB));

        UniqueCustomerList replacement = new UniqueCustomerList();
        replacement.add(ALICE);
        customers.setCustomers(replacement);
        assertEquals(replacement, customers);
    }

    @Test
    public void observableList_isUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> customers.asUnmodifiableObservableList().add(ALICE));
    }

    private static Customer customer(int id, String name) {
        return new Customer(id, new Name(name), new Phone("91234567"), new Email("test@example.com"),
                new BackupContact("98765432"));
    }
}
