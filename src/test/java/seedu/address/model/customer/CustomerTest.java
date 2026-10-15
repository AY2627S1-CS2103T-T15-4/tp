package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CustomerTest {

    private static final Name NAME = new Name("Alice Tan");
    private static final Phone PHONE = new Phone("91234567");
    private static final Email EMAIL = new Email("alice@example.com");
    private static final BackupContact BACKUP_CONTACT = new BackupContact("98765432");

    @Test
    public void constructor_invalidArguments_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Customer(-1, NAME, PHONE, EMAIL, BACKUP_CONTACT));
    }

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Customer(1, null, PHONE, EMAIL, BACKUP_CONTACT));
    }

    @Test
    public void stringConstructor_createsEquivalentCustomer() {
        Customer customer = new Customer(1, "Alice Tan", "91234567", "alice@example.com", "98765432");

        assertTrue(customer.equals(new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT)));
    }

    @Test
    public void isSameCustomer() {
        Customer customer = new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT);

        assertTrue(customer.isSameCustomer(customer));
        assertTrue(customer.isSameCustomer(new Customer(1, new Name("Bob Lee"), PHONE, EMAIL, BACKUP_CONTACT)));
        assertFalse(customer.isSameCustomer(null));
        assertFalse(customer.isSameCustomer(new Customer(2, NAME, PHONE, EMAIL, BACKUP_CONTACT)));
    }

    @Test
    public void equals() {
        Customer customer = new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT);

        assertTrue(customer.equals(new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT)));
        assertFalse(customer.equals(new Customer(1, new Name("Bob Lee"), PHONE, EMAIL, BACKUP_CONTACT)));
        assertFalse(customer.equals(null));
        assertFalse(customer.equals(1));
    }

    @Test
    public void hashCode_equalCustomers_haveSameHashCode() {
        Customer customer = new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT);

        assertEquals(customer.hashCode(), new Customer(1, NAME, PHONE, EMAIL, BACKUP_CONTACT).hashCode());
    }
}
