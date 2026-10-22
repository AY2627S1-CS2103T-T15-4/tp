package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.activity.Activity;
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
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID, LocalDate.of(2028, 2, 29))),
                addressBook.getEnrolmentList());
    }

    @Test
    public void enrolDogs_invalidRequest_rejectsWithoutChangingData() throws Exception {
        for (List<Integer> dogIds : List.<List<Integer>>of(List.of(), List.of(DOG_ID, DOG_ID), List.of(99),
                List.of(-1), Collections.singletonList(null))) {
            assertThrows(EnrolException.class, () -> addressBook.enrolDogs(ACTIVITY_ID,
                    LocalDate.of(2028, 2, 29), dogIds));
        }
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(99, LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(-1,
                LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
        assertEquals(List.of(), addressBook.getEnrolmentList());

        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(ACTIVITY_ID,
                LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
    }

    @Test
    public void unenrolDogs_invalidRequest_rejectsWithoutChangingData() throws Exception {
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        for (List<Integer> dogIds : List.of(List.<Integer>of(), List.of(DOG_ID, DOG_ID), List.of(99))) {
            assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(ACTIVITY_ID,
                    LocalDate.of(2028, 2, 29), dogIds));
        }
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(99,
                LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(-1,
                LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID, LocalDate.of(2028, 2, 29))),
                addressBook.getEnrolmentList());
    }

    @Test
    public void unenrolDogs_existingEnrolment_removesEnrolment() throws Exception {
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        addressBook.unenrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        assertEquals(List.of(), addressBook.getEnrolmentList());
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(ACTIVITY_ID,
                LocalDate.of(2028, 2, 29), List.of(DOG_ID)));
    }

    @Test
    public void enrolDogs_recurringActivityAllowsDifferentOccurrences() throws Exception {
        addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        addressBook.addDogFromStorage(new Dog(DOG_ID, new DogName("Buddy"),
                new Breed("Poodle"), new Size("Small")));

        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 3, 7), List.of(DOG_ID));

        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID, LocalDate.of(2028, 2, 29)),
                new Enrolment(ACTIVITY_ID, DOG_ID, LocalDate.of(2028, 3, 7))), addressBook.getEnrolmentList());
        assertEquals("Activity ID 1 recurs weekly on TUESDAY, starting 2028-02-29; it does not occur on 2028-03-08.",
                enrolmentException(ACTIVITY_ID, LocalDate.of(2028, 3, 8)).getMessage());
    }

    @Test
    public void enrolDogs_rejectsPastOrInvalidOccurrence() throws Exception {
        assertEquals("Activity ID 1 starts on 2028-02-29 and does not occur before that date.",
                enrolmentException(ACTIVITY_ID, LocalDate.of(2028, 2, 28)).getMessage());
        assertEquals("Activity ID 1 is a one-time activity and occurs only on 2028-02-29, not 2028-03-01.",
                enrolmentException(ACTIVITY_ID, LocalDate.of(2028, 3, 1)).getMessage());
        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(ACTIVITY_ID,
                LocalDate.of(2027, 2, 28), List.of(DOG_ID)));
        LocalDate yesterday = LocalDate.now().minusDays(1);
        addressBook.addActivity(new ActivityName("Yesterday"), yesterday,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        assertEquals("Cannot enrol on " + yesterday + ": the date must be today or later. Today is "
                        + LocalDate.now() + ".",
                enrolmentException(2, yesterday).getMessage());
    }

    @Test
    public void unenrolDogs_rejectsDateOutsideActivityRecurrence() {
        assertThrows(EnrolException.class, () -> addressBook.unenrolDogs(ACTIVITY_ID,
                LocalDate.of(2028, 3, 1), List.of(DOG_ID)));
    }

    @Test
    public void enrolDogs_rejectsOverlappingActivityButAllowsAdjacentActivity() throws Exception {
        Activity first = addressBook.getActivityList().get(0);
        addressBook.addActivity(new ActivityName("Training"), first.getStartDate(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        addressBook.addActivity(new ActivityName("Grooming"), first.getStartDate(),
                LocalTime.of(9, 30), LocalTime.of(10, 30), Recurrence.ONCE);

        addressBook.enrolDogs(1, first.getStartDate(), List.of(DOG_ID));
        addressBook.enrolDogs(2, first.getStartDate(), List.of(DOG_ID));
        String expectedMessage = "Dog ID 7 is already enrolled in \"Walk\" (activity 1, 09:00-10:00) "
                + "on 2028-02-29; this overlaps \"Grooming\" (activity 3, 09:30-10:30).";
        assertEquals(expectedMessage, enrolmentException(3, first.getStartDate()).getMessage());
        assertEquals(2, addressBook.getEnrolmentList().size());
    }

    @Test
    public void enrolDogs_conflictingBatchIsAtomic() throws Exception {
        addressBook.addDogFromStorage(new Dog(8, new DogName("Milo"),
                new Breed("Poodle"), new Size("Small")));
        addressBook.addActivity(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 30), LocalTime.of(10, 30), Recurrence.ONCE);
        addressBook.enrolDogs(2, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        assertThrows(EnrolException.class, () -> addressBook.enrolDogs(1, LocalDate.of(2028, 2, 29),
                List.of(8, DOG_ID)));
        assertEquals(List.of(new Enrolment(2, DOG_ID, LocalDate.of(2028, 2, 29))),
                addressBook.getEnrolmentList());
    }

    @Test
    public void unenrolDogs_removesOnlyRequestedOccurrence() throws Exception {
        addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        addressBook.addDogFromStorage(new Dog(DOG_ID, new DogName("Buddy"),
                new Breed("Poodle"), new Size("Small")));
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));
        addressBook.enrolDogs(ACTIVITY_ID, LocalDate.of(2028, 3, 7), List.of(DOG_ID));

        addressBook.unenrolDogs(ACTIVITY_ID, LocalDate.of(2028, 2, 29), List.of(DOG_ID));

        assertEquals(List.of(new Enrolment(ACTIVITY_ID, DOG_ID, LocalDate.of(2028, 3, 7))),
                addressBook.getEnrolmentList());
    }

    private EnrolException enrolmentException(int activityId, LocalDate date) {
        try {
            addressBook.enrolDogs(activityId, date, List.of(DOG_ID));
        } catch (EnrolException exception) {
            return exception;
        }
        throw new AssertionError("Expected enrolment to fail.");
    }
}
