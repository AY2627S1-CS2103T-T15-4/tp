package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static Customer customer(String name) {
        return new Customer(new Name(name), new Phone("91234567"), new Email("test@example.com"),
                new BackupContact("98765432"));
    }
}
