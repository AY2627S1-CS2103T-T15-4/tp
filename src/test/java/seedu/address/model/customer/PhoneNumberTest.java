package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PhoneNumberTest {
    @Test
    public void validation_localAndInternationalBoundaries_acceptsBothContactFields() {
        String[] valid = {"91234567", "9123-4567", "9  123 - 4567", "+1234567", "+123456789012345",
            "+44 20-1234-5678", "+1 202-555-0123", "+65 9123-4567"};
        for (String number : valid) {
            assertTrue(Phone.isValidPhone(number), number);
            assertTrue(BackupContact.isValidBackupContact(number), number);
        }
    }

    @Test
    public void validation_invalidLengthsOrFormatting_rejectsBothContactFields() {
        String[] invalid = {"", " ", "+", "9123456", "912345678", "+123456", "+1234567890123456",
            "+01234567", "44 20 1234 5678", "++1234567", "123+4567", "+ 1234567", "+-1234567",
            "(9123)4567", "+44 (20) 1234 5678", "9123.4567", "9123/4567", "9123a4567",
            "9123\t4567", "9123\n4567", "１２３４５６７８", " 91234567", "91234567 ", "-91234567", "91234567-"};
        for (String number : invalid) {
            assertFalse(Phone.isValidPhone(number), number);
            assertFalse(BackupContact.isValidBackupContact(number), number);
            assertThrows(IllegalArgumentException.class, () -> new Phone(number), number);
            assertThrows(IllegalArgumentException.class, () -> new BackupContact(number), number);
        }
    }

    @Test
    public void constructors_normalizeBothContactFields_andDisplayStoredValues() {
        String[][] cases = {{"9123-4567", "91234567"}, {"+65 9123-4567", "91234567"},
            {"+44 20-1234-5678", "+442012345678"}, {"+1 202-555-0123", "+12025550123"},
            {"+1234567", "+1234567"}, {"+123456789012345", "+123456789012345"}};
        for (String[] example : cases) {
            Phone phone = new Phone(example[0]);
            BackupContact backup = new BackupContact(example[0]);
            assertEquals(example[1], phone.value);
            assertEquals(example[1], backup.value);
            assertEquals(example[1], phone.toString());
            assertEquals(example[1], backup.toString());
            assertEquals(phone, new Phone(phone.value));
            assertEquals(backup, new BackupContact(backup.value));
        }
    }

    @Test
    public void equality_normalizedSingaporeAndInternationalForms_haveEqualHashes() {
        assertEquals(new Phone("91234567"), new Phone("+65 9123-4567"));
        assertEquals(new Phone("91234567").hashCode(), new Phone("+6591234567").hashCode());
        assertEquals(new BackupContact("91234567"), new BackupContact("+65 9123-4567"));
        assertEquals(new BackupContact("91234567").hashCode(), new BackupContact("+6591234567").hashCode());
        assertEquals(new Phone("+44 20-1234-5678"), new Phone("+442012345678"));
    }

    @Test
    public void constructors_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
        assertThrows(NullPointerException.class, () -> new BackupContact(null));
    }
}
