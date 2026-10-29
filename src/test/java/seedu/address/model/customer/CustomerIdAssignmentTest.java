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
            public int getNextCustomerId() {
                return 0;
            }
        });

        assertEquals(1, addressBook.getNextCustomerId());
        assertEquals(1, addressBook.addCustomer(customer("Alice")).getId());
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
