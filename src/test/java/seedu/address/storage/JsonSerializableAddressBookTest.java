package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class JsonSerializableAddressBookTest {

    @Test
    public void roundTrip_watchDogRecords_preservesLinksAndIdCounters() throws Exception {
        AddressBook original = getTypicalAddressBook();
        original.setNextDogId(80);
        original.setNextCustomerId(30);
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));

        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();

        assertEquals(original, restored);
        assertEquals(80, restored.getNextDogId());
        assertEquals(30, restored.getNextCustomerId());
        assertFalse(json.contains("\"persons\""));
    }

    @Test
    public void toModelType_legacyPersonsField_preservesWatchDogRecords() throws Exception {
        AddressBook original = getTypicalAddressBook();
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        String legacyJson = "{\"persons\":[{\"name\":\"Legacy Contact\",\"tags\":[\"friend\"]}],"
                + json.substring(json.indexOf('{') + 1);

        AddressBook restored = JsonUtil.fromJsonString(legacyJson, JsonSerializableAddressBook.class).toModelType();

        assertEquals(original, restored);
        assertEquals(original.getNextDogId(), restored.getNextDogId());
        assertEquals(original.getNextCustomerId(), restored.getNextCustomerId());
    }

    @Test
    public void toModelType_emptyOrLegacyOnlyFile_returnsEmptyBook() throws Exception {
        assertEquals(new AddressBook(), JsonUtil.fromJsonString("{}",
                JsonSerializableAddressBook.class).toModelType());
        assertEquals(new AddressBook(), JsonUtil.fromJsonString("{\"persons\":[{\"name\":\"Legacy Contact\"}]}",
                JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void toModelType_duplicateCustomers_throwsIllegalValueException() {
        Customer customer = customer(1, "Alice Tan");
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedCustomer(customer), new JsonAdaptedCustomer(customer)), null, null, null);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_CUSTOMER,
                data::toModelType);
    }

    @Test
    public void toModelType_persistedNextCustomerId_isRestored() throws Exception {
        AddressBook addressBook = new AddressBook();
        Customer firstCustomer = addressBook.addCustomer(customer(-1, "Alice Tan"));
        addressBook.removeCustomer(firstCustomer);

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(2, restored.addCustomer(customer(-1, "Bob Lim")).getId());
    }

    @Test
    public void toModelType_missingNextCustomerId_derivesIdFromStoredCustomers() throws Exception {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedCustomer(customer(7, "Alice Tan"))), null, null, null);

        AddressBook restored = data.toModelType();

        assertEquals(8, restored.addCustomer(customer(-1, "Bob Lim")).getId());
    }

    private static Customer customer(int id, String name) {
        if (id < 0) {
            return new Customer(new Name(name), new Phone("91234567"), new Email("test@example.com"),
                    new BackupContact("98765432"));
        }
        return new Customer(id, new Name(name), new Phone("91234567"), new Email("test@example.com"),
                new BackupContact("98765432"));
    }

}
