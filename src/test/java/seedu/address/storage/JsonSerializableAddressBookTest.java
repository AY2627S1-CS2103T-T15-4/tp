package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.getTypicalAddressBook;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

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
                List.of(new JsonAdaptedCustomer(customer), new JsonAdaptedCustomer(customer)),
                null, null, null, null, null);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_CUSTOMER,
                data::toModelType);
    }

    @Test
    public void toModelType_duplicateDogs_throwsIllegalValueException() {
        Dog dog = new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(null,
                List.of(new JsonAdaptedDog(dog), new JsonAdaptedDog(dog)), null, null, null, null, null);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_DOG,
                data::toModelType);
    }

    @Test
    public void toModelType_nullEnrolment_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(null, null, null, null,
                null, Collections.singletonList(null), null);

        assertThrows(IllegalValueException.class, "Enrolments list must not contain null entries.",
                data::toModelType);
    }

    @Test
    public void toModelType_enrolmentReferencesUnknownActivity_throwsIllegalValueException() {
        LocalDate date = LocalDate.of(2028, 2, 29);
        Activity activity = new Activity(1, new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(null, null, null, null,
                List.of(new JsonAdaptedActivity(activity)),
                List.of(new JsonAdaptedEnrolment(new Enrolment(2, 7, date))), null);

        assertThrows(IllegalValueException.class, "Invalid or duplicate enrolment.", data::toModelType);
    }

    @Test
    public void toModelType_negativePersistedIdCounters_derivesCountersFromRecords() throws Exception {
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedCustomer(customer(9, "Alice Tan"))),
                List.of(new JsonAdaptedDog(dog)), -1, 0, null, null, null);

        AddressBook restored = data.toModelType();

        assertEquals(10, restored.addCustomer(customer(-1, "Beth Tan")).getId());
        assertEquals(8, restored.addDog(new Dog(new DogName("Milo"), new Breed("Poodle"),
                new Size("Small"))).getId());
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
                List.of(new JsonAdaptedCustomer(customer(7, "Alice Tan"))), null, null, null, null, null);

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
