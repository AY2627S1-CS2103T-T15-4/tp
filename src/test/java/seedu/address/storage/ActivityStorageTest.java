package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

public class ActivityStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_saveAndReload_preservesAllModesAndNextId() throws Exception {
        Path file = temporaryFolder.resolve("activities.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        ModelManager model = new ModelManager();
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("addcustomer n/Alice Tan p/91234567 e/alice@example.com bc/98765432");
        logic.execute("adddog n/Cookie b/Poodle s/small");
        logic.execute("link c/1 d/1");
        for (String recurrence : List.of("once", "daily", "weekly")) {
            logic.execute("addactivity n/Training date/2028-02-29 start/09:00 end/10:00 r/" + recurrence);
        }
        ReadOnlyAddressBook saved = storage.readAddressBook().get();
        assertEquals(model.getAddressBook(), saved);
        assertEquals(4, saved.getNextActivityId());
        String jsonBefore = Files.readString(file);
        assertThrows(ParseException.class, () -> logic.execute(
                "addactivity n/Invalid date/2027-02-29 start/09:00 end/10:00 r/once"));
        assertEquals(jsonBefore, Files.readString(file));
        assertEquals(saved, model.getAddressBook());
        ModelManager reloaded = new ModelManager(saved, new UserPrefs());
        assertEquals(4, reloaded.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 3, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE).getId());
    }

    @Test
    public void toModelType_missingFieldsAndCounter_defaultsWithoutLosingExistingData() throws Exception {
        String legacy = """
                {"customers":[{"id":1,"name":"Alice Tan","phone":"91234567",
                "email":"alice@example.com","backupContact":"98765432"}],
                "dogs":[{"id":3,"name":"Cookie","breed":"Poodle","size":"small","ownerId":1}],
                "persons":[],"nextDogId":12}
                """;
        AddressBook book = JsonUtil.fromJsonString(legacy, JsonSerializableAddressBook.class).toModelType();
        assertTrue(book.getActivityList().isEmpty());
        assertEquals(1, book.getNextActivityId());
        assertEquals(12, book.getNextDogId());
        assertEquals("Alice Tan", book.getCustomerList().get(0).getName().toString());
        assertEquals(1, book.getDogList().get(0).getOwnerId().get());
        assertEquals(3, book.getDogList().get(0).getId());
        Activity activity = activity(7);
        AddressBook restored = data(List.of(new JsonAdaptedActivity(activity)), null).toModelType();
        assertEquals(List.of(activity), restored.getActivityList());
        assertEquals(8, restored.getNextActivityId());
        assertEquals(20, data(List.of(new JsonAdaptedActivity(activity)), 20L).toModelType().getNextActivityId());
    }

    @Test
    public void toModelType_invalidCollectionOrCounter_rejected() {
        JsonAdaptedActivity activity = new JsonAdaptedActivity(activity(7));
        assertThrows(IllegalValueException.class, () -> data(List.of(activity, activity), null).toModelType());
        assertThrows(IllegalValueException.class, () ->
                data(Arrays.asList((JsonAdaptedActivity) null), null).toModelType());
        for (long counter : new long[]{-1, 0, 7, (long) Integer.MAX_VALUE + 2}) {
            assertThrows(IllegalValueException.class, () -> data(List.of(activity), counter).toModelType());
        }
    }

    @Test
    public void adaptedActivity_invalidOrMissingFields_rejected() {
        String[] valid = {"Walk", "2028-02-29", "09:00", "10:00", "WEEKLY"};
        for (int i = 0; i < valid.length; i++) {
            String[] fields = valid.clone();
            fields[i] = null;
            assertThrows(IllegalValueException.class, () -> adapter(1, fields).toModelType());
        }
        assertThrows(IllegalValueException.class, () -> adapter(null, valid).toModelType());
        assertThrows(IllegalValueException.class, () -> adapter(-1, valid).toModelType());
        String[] invalid = {" ", "2027-02-29", "25:00", "08:00", "monthly"};
        for (int i = 0; i < valid.length; i++) {
            String[] fields = valid.clone();
            fields[i] = invalid[i];
            assertThrows(IllegalValueException.class, () -> adapter(1, fields).toModelType());
        }
    }

    @Test
    public void toModelType_exhaustedCounter_roundTripsWithoutOverflow() throws Exception {
        AddressBook book = new AddressBook();
        book.addActivityFromStorage(activity(Integer.MAX_VALUE));
        AddressBook restored = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)),
                JsonSerializableAddressBook.class).toModelType();
        assertEquals(book, restored);
        assertEquals((long) Integer.MAX_VALUE + 1, restored.getNextActivityId());
    }

    private JsonAdaptedActivity adapter(Integer id, String[] fields) {
        return new JsonAdaptedActivity(id, fields[0], fields[1], fields[2], fields[3], fields[4]);
    }

    private JsonSerializableAddressBook data(List<JsonAdaptedActivity> activities, Long nextId) {
        return new JsonSerializableAddressBook(null, null, null, null, null, activities, nextId);
    }

    private Activity activity(int id) {
        return new Activity(id, new ActivityName("Walk"), LocalDate.of(2028, 2, 29), LocalTime.of(9, 0),
                LocalTime.of(10, 0), Recurrence.WEEKLY);
    }
}
