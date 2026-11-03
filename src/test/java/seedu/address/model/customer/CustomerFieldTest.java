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
        assertFalse(Name.isValidName(""));
        assertFalse(Name.isValidName("   "));
        assertFalse(Name.isValidName("/Alice"));
        assertFalse(Name.isValidName("Alice\nTan"));
        assertFalse(Name.isValidName("Alice\tTan"));
        assertFalse(Name.isValidName("Alice\u0000Tan"));
        assertFalse(Name.isValidName("Alice@example.com"));
        assertFalse(Phone.isValidPhone("12a"));
        assertFalse(Phone.isValidPhone("9123456"));
        assertFalse(Phone.isValidPhone("912345678"));
        assertFalse(Phone.isValidPhone(" 91234567"));
        assertFalse(BackupContact.isValidBackupContact("12"));
        assertFalse(BackupContact.isValidBackupContact("987654321"));
        assertFalse(BackupContact.isValidBackupContact("9876543 "));
        assertFalse(Email.isValidEmail("alice@example.c"));
        assertFalse(Email.isValidEmail(".alice@example.com"));
        assertFalse(Email.isValidEmail("alice.@example.com"));
        assertFalse(Email.isValidEmail("alice..tan@example.com"));
    }

    @Test
    public void emails_allowSupportedSpecialCharactersExceptMisplacedDots() {
        for (String local : new String[]{"_alice", "alice+", "alice+_work", "o''neil", "'oneil",
            "-alice", "alice_", "alice'", "alice-", "+_'-", "alice._work"}) {
            assertTrue(Email.isValidEmail(local + "@example.com"), local);
        }
        for (String local : new String[]{"", ".alice", "alice.", "alice..tan", "alice!work", "alice/bob",
            "alice&bob", "alice=work", "\"john doe\"", "alice tan", "alice@work"}) {
            assertFalse(Email.isValidEmail(local + "@example.com"), local);
        }
        assertFalse(Email.isValidEmail("alice@-example.com"));
        assertFalse(Email.isValidEmail("alice@example..com"));
    }

    @Test
    public void names_acceptUnicodeAndSlashesAndNormalizeAccents() {
        for (String name : new String[]{"Ahmad s/o Ali", "Devi d/o Kumar", "Jos\u00e9 Tan", "\u9648\u4f1f",
            "O\u2019Neil", "Jose\u0301 Tan", "Jean--Luc", "O''Neil", "Jean-", "Alice 2"}) {
            assertTrue(Name.isValidName(name), name);
        }
        assertEquals(new Name("Jos\u00e9 Tan"), new Name("Jose\u0301 Tan"));
        assertEquals("Jos\u00e9 Tan", new Name("Jose\u0301 Tan").fullName);
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
