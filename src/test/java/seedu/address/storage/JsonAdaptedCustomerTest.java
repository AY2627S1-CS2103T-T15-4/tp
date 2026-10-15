package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedCustomer.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class JsonAdaptedCustomerTest {

    private static final int VALID_ID = 1;
    private static final String VALID_NAME = "Alice Tan";
    private static final String VALID_PHONE = "91234567";
    private static final String VALID_EMAIL = "alice@example.com";
    private static final String VALID_BACKUP_CONTACT = "98765432";

    @Test
    public void toModelType_validCustomerDetails_returnsCustomer() throws Exception {
        Customer expected = new Customer(VALID_ID, new Name(VALID_NAME), new Phone(VALID_PHONE),
                new Email(VALID_EMAIL), new BackupContact(VALID_BACKUP_CONTACT));

        assertEquals(expected, new JsonAdaptedCustomer(expected).toModelType());
    }

    @Test
    public void toModelType_missingFields_throwIllegalValueException() {
        assertMissingField(null, VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_BACKUP_CONTACT, "id");
        assertMissingField(VALID_ID, null, VALID_PHONE, VALID_EMAIL, VALID_BACKUP_CONTACT, "name");
        assertMissingField(VALID_ID, VALID_NAME, null, VALID_EMAIL, VALID_BACKUP_CONTACT, "phone");
        assertMissingField(VALID_ID, VALID_NAME, VALID_PHONE, null, VALID_BACKUP_CONTACT, "email");
        assertMissingField(VALID_ID, VALID_NAME, VALID_PHONE, VALID_EMAIL, null, "backupContact");
    }

    @Test
    public void toModelType_negativeId_throwsIllegalValueException() {
        JsonAdaptedCustomer customer = new JsonAdaptedCustomer(-1, VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_BACKUP_CONTACT);

        assertThrows(IllegalValueException.class, "Customer id must be nonnegative.", customer::toModelType);
    }

    private static void assertMissingField(Integer id, String name, String phone, String email, String backupContact,
            String missingField) {
        JsonAdaptedCustomer customer = new JsonAdaptedCustomer(id, name, phone, email, backupContact);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, missingField);

        assertThrows(IllegalValueException.class, expectedMessage, customer::toModelType);
    }
}
