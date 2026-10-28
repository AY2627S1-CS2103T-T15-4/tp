package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CustomerFieldTest {

    @Test
    public void validation_acceptsValidValues() {
        assertTrue(Name.isValidName("Alice Tan 2"));
        assertTrue(Name.isValidName("Jean-Luc"));
        assertTrue(Name.isValidName("O'Neil"));
        assertTrue(Phone.isValidPhone("91234567"));
        assertTrue(Phone.isValidPhone("9 1 2 3 4 5 6 7"));
        assertTrue(BackupContact.isValidBackupContact("98765432"));
        assertTrue(BackupContact.isValidBackupContact("9 8 7 6 5 4 3 2"));
        assertTrue(Email.isValidEmail("alice.tan@example.com"));
        assertTrue(Email.isValidEmail("o'neil@example.com"));
    }

    @Test
    public void validation_rejectsInvalidValues() {
        assertFalse(Name.isValidName(" Alice"));
        assertFalse(Name.isValidName("Jean--Luc"));
        assertFalse(Name.isValidName("O''Neil"));
        assertFalse(Name.isValidName("Jean-"));
        assertFalse(Phone.isValidPhone("12a"));
        assertFalse(Phone.isValidPhone("9123456"));
        assertFalse(Phone.isValidPhone("912345678"));
        assertFalse(Phone.isValidPhone(" 91234567"));
        assertFalse(BackupContact.isValidBackupContact("12"));
        assertFalse(BackupContact.isValidBackupContact("987654321"));
        assertFalse(BackupContact.isValidBackupContact("9876543 "));
        assertFalse(Email.isValidEmail("alice@example.c"));
        assertFalse(Email.isValidEmail("o''neil@example.com"));
        assertFalse(Email.isValidEmail("'oneil@example.com"));
    }

    @Test
    public void phoneConstructors_removeSpacesBetweenDigits() {
        assertEquals("91234567", new Phone("9 1 2 3 4 5 6 7").value);
        assertEquals("98765432", new BackupContact("9 8 7 6 5 4 3 2").value);
    }

    @Test
    public void constructors_rejectNullAndInvalidValues() {
        assertThrows(NullPointerException.class, () -> new Name(null));
        assertThrows(IllegalArgumentException.class, () -> new Phone("abc"));
        assertThrows(IllegalArgumentException.class, () -> new BackupContact("12"));
        assertThrows(IllegalArgumentException.class, () -> new Email("invalid"));
    }

    @Test
    public void equality_isBasedOnValueAndType() {
        assertTrue(new Name("Alice").equals(new Name("Alice")));
        assertFalse(new Name("Alice").equals("Alice"));
        assertTrue(new BackupContact("91234567").equals(new BackupContact("91234567")));
        assertFalse(new BackupContact("91234567").equals(new Phone("91234567")));
    }
}
