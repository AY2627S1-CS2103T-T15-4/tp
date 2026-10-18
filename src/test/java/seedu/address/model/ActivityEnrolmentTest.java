package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.model.dog.exceptions.EnrolException;

/** Tests enrolment and unenrolment operations in the address book. */
public class ActivityEnrolmentTest {
    private static final int ACTIVITY_ID = 1;
    private static final int DOG_ID = 7;

    private AddressBook addressBook;

    @BeforeEach
    public void setUp() {
        addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        addressBook.addDogFromStorage(new Dog(DOG_ID, new DogName("Buddy"),
                new Breed("Poodle"), new Size("Small")));
    }

    @Test
    public void enrolDogs_validRequest_addsEnrolment() throws Exception {
        addressBook.enrolDogs(ACTIVITY_ID, List.of(DOG_ID));

        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID)), addressBook.getEnrolmentList());
    }

    @Test
    public void enrolDogs_invalidRequest_rejectsWithoutChangingData() throws Exception {
        for (List<Integer> dogIds : List.of(List.<Integer>of(), List.of(DOG_ID, DOG_ID), List.of(99))) {
            assertThrows(EnrolException.class, () -> addressBook.enrolDogs(ACTIVITY_ID, dogIds));
        }
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(99, List.of(DOG_ID)));
        assertEquals(List.of(), addressBook.getEnrolmentList());

        addressBook.enrolDogs(ACTIVITY_ID, List.of(DOG_ID));
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(ACTIVITY_ID, List.of(DOG_ID)));
    }

    @Test
    public void unenrolDogs_invalidRequest_rejectsWithoutChangingData() throws Exception {
        addressBook.enrolDogs(ACTIVITY_ID, List.of(DOG_ID));

        for (List<Integer> dogIds : List.of(List.<Integer>of(), List.of(DOG_ID, DOG_ID), List.of(99))) {
            assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(ACTIVITY_ID, dogIds));
        }
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(99, List.of(DOG_ID)));
        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID)), addressBook.getEnrolmentList());
    }

    @Test
    public void unenrolDogs_existingEnrolment_removesEnrolment() throws Exception {
        addressBook.enrolDogs(ACTIVITY_ID, List.of(DOG_ID));

        addressBook.unenrolDogs(ACTIVITY_ID, List.of(DOG_ID));

        assertEquals(List.of(), addressBook.getEnrolmentList());
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(ACTIVITY_ID, List.of(DOG_ID)));
    }
}
