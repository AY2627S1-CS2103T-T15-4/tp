package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests persistence of activity enrolments. */
public class EnrolmentStorageTest {
    @Test
    public void roundTrip_enrolments_preservesRecords() throws Exception {
        AddressBook source = new AddressBook();
        Activity activity = source.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        source.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        source.enrolDogs(activity.getId(), List.of(7));

        AddressBook restored = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(source)),
                JsonSerializableAddressBook.class).toModelType();

        assertEquals(source.getEnrolmentList(), restored.getEnrolmentList());
        assertEquals(source, restored);
    }

    @Test
    public void roundTrip_deletedActivity_doesNotRestoreActivityOrEnrolment() throws Exception {
        AddressBook source = new AddressBook();
        Activity activity = source.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        source.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        source.enrolDogs(activity.getId(), List.of(7));
        source.removeActivity(activity);

        AddressBook restored = JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonSerializableAddressBook(source)),
                JsonSerializableAddressBook.class).toModelType();

        assertEquals(List.of(), restored.getActivityList());
        assertEquals(List.of(), restored.getEnrolmentList());
        assertEquals(source.getNextActivityId(), restored.getNextActivityId());
        assertEquals(source.getDogList(), restored.getDogList());
    }

    @Test
    public void load_invalidOrDuplicateEnrolments_rejectsData() {
        JsonAdaptedActivity activity = new JsonAdaptedActivity(1, "Walk", "2028-02-29",
                "09:00", "10:00", "ONCE");
        JsonAdaptedDog dog = new JsonAdaptedDog(7, "Buddy", "Poodle", "Small", null);
        JsonAdaptedEnrolment valid = new JsonAdaptedEnrolment(1, 7);

        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                null, List.of(dog), null, null, List.of(activity), List.of(valid, valid), 2L).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonSerializableAddressBook(
                null, List.of(dog), null, null, List.of(activity), List.of(new JsonAdaptedEnrolment(1, 8)),
                2L).toModelType());
    }

    @Test
    public void enrolment_negativeId_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Enrolment(-1, 7));
        assertThrows(IllegalArgumentException.class, () -> new Enrolment(1, -1));
    }
}
