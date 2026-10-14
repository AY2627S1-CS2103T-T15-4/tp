package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CustomerFieldTest {

    @Test
    public void validation_acceptsValidValues() {
        assertTrue(Name.isValidName("Alice Tan 2"));
        assertTrue(Phone.isValidPhone("91234567"));
        assertTrue(BackupContact.isValidBackupContact("98765432"));
        assertTrue(Email.isValidEmail("alice.tan@example.com"));
        assertTrue(Address.isValidAddress("123 Clementi Ave 2"));
    }

    @Test
    public void validation_rejectsInvalidValues() {
        assertFalse(Name.isValidName(" Alice"));
        assertFalse(Phone.isValidPhone("12a"));
        assertFalse(BackupContact.isValidBackupContact("12"));
        assertFalse(Email.isValidEmail("alice@example.c"));
        assertFalse(Address.isValidAddress(" "));
    }

    @Test
    public void constructors_rejectNullAndInvalidValues() {
        assertThrows(NullPointerException.class, () -> new Name(null));
        assertThrows(IllegalArgumentException.class, () -> new Phone("abc"));
        assertThrows(IllegalArgumentException.class, () -> new BackupContact("12"));
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid"));
        assertThrows(IllegalArgumentException.class, () -> new Address(" "));
    }

    @Test
    public void equality_isBasedOnValueAndType() {
        assertTrue(new Name("Alice").equals(new Name("Alice")));
        assertFalse(new Name("Alice").equals(new seedu.address.model.person.Name("Alice")));
        assertTrue(new BackupContact("91234567").equals(new BackupContact("91234567")));
        assertFalse(new BackupContact("91234567").equals(new Phone("91234567")));
    }
}
