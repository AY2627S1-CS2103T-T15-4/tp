package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;

public class CustomerIdAssignmentTest {

    @Test
    public void addCustomer_assignsIncreasingIdsWithoutReusingDeletedId() {
        AddressBook addressBook = new AddressBook();
        Customer firstCustomer = addressBook.addCustomer(customer("Alice"));
        Customer secondCustomer = addressBook.addCustomer(customer("Bob"));

        addressBook.removeCustomer(firstCustomer);
        Customer thirdCustomer = addressBook.addCustomer(customer("Carol"));

        assertEquals(1, firstCustomer.getId());
        assertEquals(2, secondCustomer.getId());
        assertEquals(3, thirdCustomer.getId());
    }

    @Test
    public void addressBookCopy_preservesNextCustomerId() {
        AddressBook original = new AddressBook();
        Customer firstCustomer = original.addCustomer(customer("Alice"));
        original.removeCustomer(firstCustomer);

        AddressBook copy = new AddressBook(original);
        Customer secondCustomer = copy.addCustomer(customer("Bob"));

        assertEquals(2, secondCustomer.getId());
    }

    @Test
    public void addCustomer_existingId_advancesCounterForLaterUnassignedCustomer() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(new Customer(7, "Stored", "91234567", "stored@example.com", "98765432"));

        Customer assignedCustomer = addressBook.addCustomer(customer("New Customer"));

        assertEquals(8, assignedCustomer.getId());
    }

    @Test
    public void resetData_zeroNextCustomerId_recomputesPositiveId() {
        AddressBook addressBook = new AddressBook(new AddressBook() {
            @Override
            public long getNextCustomerId() {
                return 0;
            }
        });

        assertEquals(1, addressBook.getNextCustomerId());
        assertEquals(1, addressBook.addCustomer(customer("Alice")).getId());
    }

    @Test
    public void addCustomer_maximumId_exhaustsIdsWithoutReusingDeletedId() {
        AddressBook addressBook = new AddressBook();
        addressBook.setNextCustomerId(Integer.MAX_VALUE);

        Customer lastCustomer = addressBook.addCustomer(customer("Alice"));

        assertEquals(Integer.MAX_VALUE, lastCustomer.getId());
        assertEquals((long) Integer.MAX_VALUE + 1, addressBook.getNextCustomerId());
        assertThrows(IllegalStateException.class,
                "No more customer IDs are available", () -> addressBook.addCustomer(customer("Bob")));
        assertEquals(1, addressBook.getCustomerList().size());

        addressBook.removeCustomer(lastCustomer);
        AddressBook copy = new AddressBook(addressBook);
        assertThrows(IllegalStateException.class,
                "No more customer IDs are available", () -> copy.addCustomer(customer("Carol")));
        assertEquals(0, copy.getCustomerList().size());
    }

    @Test
    public void setNextCustomerId_aboveExhaustedCounter_rejectsWithoutChangingCounter() {
        AddressBook addressBook = new AddressBook();
        long exhaustedCounter = (long) Integer.MAX_VALUE + 1;
        addressBook.setNextCustomerId(exhaustedCounter);

        assertThrows(IllegalArgumentException.class, "Invalid next customer ID.", () ->
                addressBook.setNextCustomerId(exhaustedCounter + 1));

        assertEquals(exhaustedCounter, addressBook.getNextCustomerId());
        assertThrows(IllegalStateException.class, "No more customer IDs are available", () ->
                addressBook.addCustomer(customer("Alice")));
    }

    @Test
    public void setNextCustomerId_existingCustomerId_rejectsWithoutChangingCounter() {
        AddressBook addressBook = new AddressBook();
        Customer firstCustomer = addressBook.addCustomer(customer("Alice"));

        assertThrows(IllegalArgumentException.class, "Invalid next customer ID.", () ->
                addressBook.setNextCustomerId(firstCustomer.getId()));

        assertEquals(2, addressBook.addCustomer(customer("Bob")).getId());
    }

    @Test
    public void setNextCustomerId_rejectsNonPositiveId() {
        AddressBook addressBook = new AddressBook();

        assertThrows(IllegalArgumentException.class, () -> addressBook.setNextCustomerId(0));
        addressBook.setNextCustomerId(10);
        assertEquals(10, addressBook.addCustomer(customer("Alice")).getId());
    }

    private static Customer customer(String name) {
        return new Customer(new Name(name), new Phone("91234567"), new Email("test@example.com"),
                new BackupContact("98765432"));
    }
}
