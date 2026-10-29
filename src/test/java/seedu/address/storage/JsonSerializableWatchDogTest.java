package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.getTypicalWatchDog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.WatchDog;
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

public class JsonSerializableWatchDogTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void roundTrip_watchDogRecords_preservesLinksAndIdCounters() throws Exception {
        WatchDog original = getTypicalWatchDog();
        original.setNextDogId(80);
        original.setNextCustomerId(30);
        String json = JsonUtil.toJsonString(new JsonSerializableWatchDog(original));

        WatchDog restored = JsonUtil.fromJsonString(json, JsonSerializableWatchDog.class).toModelType();

        // @@author Kenneth-Chia
        assertEquals(original, restored);
        assertEquals(80, restored.getNextDogId());
        assertEquals(30, restored.getNextCustomerId());
        assertFalse(json.contains("\"persons\""));
        // @@author
    }

    @Test
    public void toModelType_legacyPersonsField_preservesWatchDogRecords() throws Exception {
        WatchDog original = getTypicalWatchDog();
        String json = JsonUtil.toJsonString(new JsonSerializableWatchDog(original));
        String legacyJson = "{\"persons\":[{\"name\":\"Legacy Contact\",\"tags\":[\"friend\"]}],"
                + json.substring(json.indexOf('{') + 1);

        WatchDog restored = JsonUtil.fromJsonString(legacyJson, JsonSerializableWatchDog.class).toModelType();

        // @@author Kenneth-Chia
        assertEquals(original, restored);
        assertEquals(original.getNextDogId(), restored.getNextDogId());
        assertEquals(original.getNextCustomerId(), restored.getNextCustomerId());
        // @@author
    }

    @Test
    public void toModelType_emptyOrLegacyOnlyFile_returnsEmptyBook() throws Exception {
        assertEquals(new WatchDog(), JsonUtil.fromJsonString("{}",
                JsonSerializableWatchDog.class).toModelType());
        assertEquals(new WatchDog(), JsonUtil.fromJsonString("{\"persons\":[{\"name\":\"Legacy Contact\"}]}",
                JsonSerializableWatchDog.class).toModelType());
    }

    @Test
    public void toModelType_nullCustomerOrDog_throwsIllegalValueException() throws Exception {
        assertThrows(IllegalValueException.class, "Customers list must not contain null entries.", () ->
                JsonUtil.fromJsonString("{\"customers\":[null]}", JsonSerializableWatchDog.class).toModelType());
        assertThrows(IllegalValueException.class, "Dog list must not contain null entries.", () ->
                JsonUtil.fromJsonString("{\"dogs\":[null]}", JsonSerializableWatchDog.class).toModelType());
    }

    @Test
    public void toModelType_duplicateCustomers_throwsIllegalValueException() {
        Customer customer = customer(1, "Alice Tan");
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(
                List.of(new JsonAdaptedCustomer(customer), new JsonAdaptedCustomer(customer)),
                null, null, null, null, null);

        assertThrows(IllegalValueException.class, JsonSerializableWatchDog.MESSAGE_DUPLICATE_CUSTOMER,
                data::toModelType);
    }

    @Test
    public void toModelType_duplicateDogs_throwsIllegalValueException() {
        Dog dog = new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(null,
                List.of(new JsonAdaptedDog(dog), new JsonAdaptedDog(dog)), null, null, null, null, null);

        assertThrows(IllegalValueException.class, JsonSerializableWatchDog.MESSAGE_DUPLICATE_DOG,
                data::toModelType);
    }

    @Test
    public void toModelType_nullEnrolment_throwsIllegalValueException() {
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(null, null, null, null,
                null, Collections.singletonList(null), null);

        assertThrows(IllegalValueException.class, "Enrolments list must not contain null entries.",
                data::toModelType);
    }

    @Test
    public void toModelType_enrolmentReferencesUnknownActivity_throwsIllegalValueException() {
        LocalDate date = LocalDate.of(2028, 2, 29);
        Activity activity = new Activity(1, new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(null, null, null, null,
                List.of(new JsonAdaptedActivity(activity)),
                List.of(new JsonAdaptedEnrolment(new Enrolment(2, 7, date))), null);

        assertThrows(IllegalValueException.class, "Invalid or duplicate enrolment.", data::toModelType);
    }

    @Test
    public void toModelType_missingDogCounter_derivesCounterFromRecords() throws Exception {
        String legacyJson = """
                {"customers":[{"id":9,"name":"Alice Tan","phone":"91234567",
                "email":"test@example.com","backupContact":"98765432"}],
                "dogs":[{"id":7,"name":"Buddy","breed":"Poodle","size":"Small"}],
                "nextCustomerId":0}
                """;


        WatchDog restored = JsonUtil.fromJsonString(
                legacyJson, JsonSerializableWatchDog.class).toModelType();

        assertEquals(10, restored.addCustomer(customer(-1, "Beth Tan")).getId());
        assertEquals(8, restored.addDog(new Dog(new DogName("Milo"), new Breed("Poodle"),
                new Size("Small"))).getId());
    }

    @Test
    public void toModelType_invalidPersistedDogCounter_throwsIllegalValueException() {
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        for (long counter : new long[]{-1, 0, 6, 7, (long) Integer.MAX_VALUE + 2}) {
            JsonSerializableWatchDog data = new JsonSerializableWatchDog(null,
                    List.of(new JsonAdaptedDog(dog)), counter, null, null, null, null);

            assertThrows(IllegalValueException.class, data::toModelType);
        }
    }

    @Test
    public void toModelType_exhaustedDogCounter_roundTripsWithoutOverflow() throws Exception {
        WatchDog original = new WatchDog();
        original.addDogFromStorage(new Dog(Integer.MAX_VALUE, new DogName("Buddy"),
                new Breed("Poodle"), new Size("Small")));

        String json = JsonUtil.toJsonString(new JsonSerializableWatchDog(original));
        WatchDog restored = JsonUtil.fromJsonString(json, JsonSerializableWatchDog.class).toModelType();

        assertEquals(original, restored);
        assertEquals((long) Integer.MAX_VALUE + 1, restored.getNextDogId());
    }

    @Test
    public void readWatchDog_nonIntegerDogCounter_rejectedWithoutRewritingFile() throws Exception {
        Path file = temporaryFolder.resolve("invalid-dog-counter.json");
        JsonWatchDogStorage storage = new JsonWatchDogStorage(file);
        for (String value : List.of("1.9", "1.0", "1e0", "\"1\"", "true", "{}", "[]",
                "9223372036854775808")) {
            String json = "{\"nextDogId\":" + value + "}";
            Files.writeString(file, json);

            assertThrows(DataLoadingException.class, storage::readWatchDog);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void toModelType_persistedNextCustomerId_isRestored() throws Exception {
        WatchDog watchDog = new WatchDog();
        Customer firstCustomer = watchDog.addCustomer(customer(-1, "Alice Tan"));
        watchDog.removeCustomer(firstCustomer);

        WatchDog restored = new JsonSerializableWatchDog(watchDog).toModelType();

        assertEquals(2, restored.addCustomer(customer(-1, "Bob Lim")).getId());
    }

    @Test
    public void toModelType_legacyMaximumCustomerId_preservesExhaustion() throws Exception {
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(
                List.of(new JsonAdaptedCustomer(customer(Integer.MAX_VALUE, "Alice Tan"))),
                null, null, (long) Integer.MAX_VALUE, null, null);

        WatchDog restored = data.toModelType();

        assertEquals((long) Integer.MAX_VALUE + 1, restored.getNextCustomerId());
        assertThrows(IllegalStateException.class,
                "No more customer IDs are available", () -> restored.addCustomer(customer(-1, "Bob Lim")));
    }

    @Test
    public void toModelType_nextCustomerIdAboveExhaustedCounter_throwsIllegalValueException() throws Exception {
        String json = "{\"nextCustomerId\":2147483649}";
        JsonSerializableWatchDog data = JsonUtil.fromJsonString(json, JsonSerializableWatchDog.class);

        assertThrows(IllegalValueException.class, "Invalid next customer ID.", data::toModelType);
    }

    @Test
    public void toModelType_missingNextCustomerId_derivesIdFromStoredCustomers() throws Exception {
        JsonSerializableWatchDog data = new JsonSerializableWatchDog(
                List.of(new JsonAdaptedCustomer(customer(7, "Alice Tan"))), null, null, null, null, null);

        WatchDog restored = data.toModelType();

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
