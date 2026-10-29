package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.ALICE;
import static seedu.address.testutil.TypicalWatchDog.MAX;
import static seedu.address.testutil.TypicalWatchDog.getTypicalWatchDog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class WatchDogTest {

    private final WatchDog watchDog = new WatchDog();

    @Test
    public void constructor() {
        assertEquals(List.of(), watchDog.getCustomerList());
        assertEquals(List.of(), watchDog.getDogList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> watchDog.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyWatchDog_replacesData() {
        WatchDog newData = getTypicalWatchDog();
        watchDog.resetData(newData);
        assertEquals(newData, watchDog);
    }

    @Test
    public void resetData_restoresActivitiesAndTheirEnrolments() throws Exception {
        WatchDog source = new WatchDog();
        LocalDate date = LocalDate.of(2028, 2, 29);
        Activity activity = source.addActivity(new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        source.addDogFromStorage(dog);
        source.enrolDogs(activity.getId(), date, List.of(dog.getId()));

        watchDog.resetData(source);

        assertEquals(List.of(activity), watchDog.getActivityList());
        assertEquals(List.of(new Enrolment(activity.getId(), dog.getId(), date)), watchDog.getEnrolmentList());
    }

    @Test
    public void resetData_withoutPersistedDogAndCustomerCounters_derivesNextIds() {
        Customer customer = new Customer(9, new Name("Alex Tan"), new Phone("91234567"),
                new Email("alex@example.com"), new BackupContact("98765432"));
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        ReadOnlyWatchDog legacyData = new ReadOnlyWatchDog() {
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.observableArrayList(customer);
            }

            @Override
            public ObservableList<Dog> getDogList() {
                return FXCollections.observableArrayList(dog);
            }
        };

        watchDog.resetData(legacyData);

        assertEquals(10, watchDog.addCustomer(new Customer(new Name("Beth Tan"), new Phone("92345678"),
                new Email("beth@example.com"), new BackupContact("97654321"))).getId());
        assertEquals(8, watchDog.addDog(new Dog(new DogName("Milo"), new Breed("Poodle"),
                new Size("Large"))).getId());
    }

    @Test
    public void setActivity_invalidIdentityOrStaleTarget_rejectsWithoutChangingActivity() {
        Activity original = watchDog.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        Activity differentId = new Activity(2, original.getName(), original.getStartDate(),
                original.getStartTime(), original.getEndTime(), original.getRecurrence());
        Activity staleTarget = original.withName(new ActivityName("Old name"));
        Activity renamed = original.withName(new ActivityName("Training"));

        assertThrows(IllegalArgumentException.class, () -> watchDog.setActivity(original, differentId));
        assertThrows(IllegalArgumentException.class, () -> watchDog.setActivity(staleTarget, renamed));
        assertEquals(List.of(original), watchDog.getActivityList());
    }

    @Test
    public void setActivity_nonexistentTarget_rejectsWithoutChangingActivity() {
        Activity original = watchDog.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        Activity missing = new Activity(2, new ActivityName("Training"), LocalDate.of(2028, 3, 1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);

        assertThrows(IllegalArgumentException.class, () -> watchDog.setActivity(missing, missing));
        assertEquals(List.of(original), watchDog.getActivityList());
    }

    @Test
    public void setActivity_withoutEnrolments_allowsScheduleChange() {
        Activity original = watchDog.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        Activity edited = original.withSchedule(original.getStartDate(), LocalTime.of(8, 0),
                LocalTime.of(9, 0), original.getRecurrence());

        watchDog.setActivity(original, edited);
        assertEquals(List.of(edited), watchDog.getActivityList());
    }

    @Test
    public void setActivity_withEnrolment_rejectsAndRetainsActivityAndEnrolment() throws Exception {
        LocalDate date = LocalDate.of(2028, 2, 29);
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        watchDog.addDogFromStorage(dog);
        Activity target = watchDog.addActivity(new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        watchDog.enrolDogs(target.getId(), date, List.of(dog.getId()));
        Activity edited = target.withSchedule(date, LocalTime.of(8, 0),
                LocalTime.of(9, 0), target.getRecurrence());

        String expectedMessage = "Activity ID " + target.getId()
                + " has enrolments and cannot be edited. Create a new activity instead.";
        assertThrows(IllegalArgumentException.class, expectedMessage, () ->
                watchDog.setActivity(target, edited));
        assertEquals(target, watchDog.getActivityList().get(0));
        assertEquals(List.of(new Enrolment(target.getId(), dog.getId(), date)), watchDog.getEnrolmentList());
    }

    @Test
    public void setActivity_withEnrolmentOnlyForAnotherActivity_editsTargetAndPreservesEnrolment() throws Exception {
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        watchDog.addDogFromStorage(dog);
        Activity target = watchDog.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity other = watchDog.addActivity(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        watchDog.enrolDogs(other.getId(), other.getStartDate(), List.of(dog.getId()));
        Activity edited = target.withSchedule(target.getStartDate(), LocalTime.of(12, 0),
                LocalTime.of(13, 0), target.getRecurrence());

        watchDog.setActivity(target, edited);

        assertEquals(List.of(edited, other), watchDog.getActivityList());
        assertEquals(List.of(new Enrolment(other.getId(), dog.getId(), other.getStartDate())),
                watchDog.getEnrolmentList());
    }

    @Test
    public void setActivity_atMaximumDateWithEnrolment_rejectsWithoutChangingData() throws Exception {
        Dog dog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
        watchDog.addDogFromStorage(dog);
        Activity target = watchDog.addActivity(new ActivityName("Walk"), LocalDate.MAX,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity previousDay = watchDog.addActivity(new ActivityName("Training"), LocalDate.MAX.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        watchDog.enrolDogs(target.getId(), target.getStartDate(), List.of(dog.getId()));
        watchDog.addEnrolmentFromStorage(new Enrolment(previousDay.getId(), dog.getId(),
                previousDay.getStartDate()));
        Activity edited = target.withSchedule(target.getStartDate(), LocalTime.of(8, 0),
                LocalTime.of(10, 0), target.getRecurrence());

        List<Activity> originalActivities = List.copyOf(watchDog.getActivityList());
        List<Enrolment> originalEnrolments = List.copyOf(watchDog.getEnrolmentList());

        assertThrows(IllegalArgumentException.class, () -> watchDog.setActivity(target, edited));
        assertEquals(originalActivities, watchDog.getActivityList());
        assertEquals(originalEnrolments, watchDog.getEnrolmentList());
    }

    @Test
    public void addEnrolmentFromStorage_duplicate_rejectsDuplicate() {
        LocalDate date = LocalDate.of(2028, 2, 29);
        watchDog.addActivity(new ActivityName("Walk"), date, LocalTime.of(9, 0),
                LocalTime.of(10, 0), Recurrence.ONCE);
        watchDog.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Enrolment enrolment = new Enrolment(1, 7, date);
        watchDog.addEnrolmentFromStorage(enrolment);

        assertThrows(IllegalArgumentException.class, () -> watchDog.addEnrolmentFromStorage(enrolment));
        assertEquals(List.of(enrolment), watchDog.getEnrolmentList());
    }

    @Test
    public void addEnrolmentFromStorage_nonOccurrence_rejectsEnrolment() {
        LocalDate date = LocalDate.of(2028, 2, 29);
        Activity activity = watchDog.addActivity(new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        watchDog.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));

        assertThrows(IllegalArgumentException.class, () -> watchDog.addEnrolmentFromStorage(
                new Enrolment(activity.getId(), 7, date.plusDays(1))));
        assertTrue(watchDog.getEnrolmentList().isEmpty());
    }

    @Test
    public void addDog_assignsIncreasingIds() {
        Dog firstDog = watchDog.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));
        Dog secondDog = watchDog.addDog(new Dog(new DogName("Milo"),
                new Breed("Poodle"), new Size("Small")));

        assertEquals(1, firstDog.getId());
        assertEquals(2, secondDog.getId());
        assertEquals(List.of(firstDog, secondDog), watchDog.getDogList());
    }

    @Test
    public void constructor_zeroId_rejectsDog() {
        assertThrows(IllegalArgumentException.class, "Dog id must be positive.", () ->
                new Dog(0, new DogName("Cookie"), new Breed("Poodle"), new Size("Small")));
    }

    @Test
    public void addDogFromStorage_preservesIdAndAdvancesCounter() {
        Dog storedDog = new Dog(7, new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large"));
        watchDog.addDogFromStorage(storedDog);

        Dog addedDog = watchDog.addDog(new Dog(new DogName("Milo"),
                new Breed("Poodle"), new Size("Small")));

        assertEquals(7, watchDog.getDogList().get(0).getId());
        assertEquals(8, addedDog.getId());
    }

    @Test
    public void addDogFromStorage_maximumId_setsExhaustedCounter() {
        Dog storedDog = new Dog(Integer.MAX_VALUE, new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large"));

        watchDog.addDogFromStorage(storedDog);

        assertEquals((long) Integer.MAX_VALUE + 1, watchDog.getNextDogId());
    }

    @Test
    public void addDog_lastAvailableId_doesNotOverflowOrMutateAfterExhaustion() {
        watchDog.setNextDogId(Integer.MAX_VALUE);
        Dog added = watchDog.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));

        assertEquals(Integer.MAX_VALUE, added.getId());
        assertEquals((long) Integer.MAX_VALUE + 1, watchDog.getNextDogId());
        WatchDog beforeRejectedAddition = new WatchDog(watchDog);

        assertThrows(IllegalStateException.class, "No more dog IDs are available.", () ->
                watchDog.addDog(new Dog(new DogName("Milo"), new Breed("Poodle"), new Size("Small"))));
        assertEquals(beforeRejectedAddition, watchDog);
    }

    @Test
    public void enrolDogs_zeroDogId_rejectsInvalidId() throws Exception {
        LocalDate date = LocalDate.of(2028, 2, 29);
        Activity activity = watchDog.addActivity(new ActivityName("Walk"), date,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);

        assertThrows(seedu.address.model.dog.exceptions.EnrolException.class,
                "Dog IDs must be positive whole numbers.", () ->
                        watchDog.enrolDogs(activity.getId(), date, List.of(0)));
        assertThrows(seedu.address.model.dog.exceptions.EnrolException.class,
                "Dog IDs must be positive whole numbers.", () ->
                        watchDog.unenrolDogs(activity.getId(), date, List.of(0)));
    }

    @Test
    public void setNextDogId_invalidCounter_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> watchDog.setNextDogId(-1));
        assertThrows(IllegalArgumentException.class,
                "Next dog ID must be positive, exceed all stored dog IDs, and be within range.", () ->
                watchDog.setNextDogId(0));
        assertThrows(IllegalArgumentException.class, () ->
                watchDog.setNextDogId((long) Integer.MAX_VALUE + 2));
    }

    @Test
    public void resetData_emptyBookWithZeroCounter_rejectsCounter() {
        ReadOnlyWatchDog legacyData = new ReadOnlyWatchDog() {
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.observableArrayList();
            }

            @Override
            public long getNextDogId() {
                return 0;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> watchDog.resetData(legacyData));
        assertEquals(1, watchDog.getNextDogId());
    }

    @Test
    public void resetData_counterNotAboveStoredDogs_rejectsWithoutMutation() {
        watchDog.addDog(new Dog(new DogName("Existing"), new Breed("Poodle"), new Size("Small")));
        WatchDog before = new WatchDog(watchDog);
        Dog incomingDog = new Dog(7, new DogName("Incoming"), new Breed("Beagle"), new Size("Medium"));
        ReadOnlyWatchDog invalidData = new ReadOnlyWatchDog() {
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.observableArrayList();
            }

            @Override
            public ObservableList<Dog> getDogList() {
                return FXCollections.observableArrayList(incomingDog);
            }

            @Override
            public long getNextDogId() {
                return 7;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> watchDog.resetData(invalidData));
        assertEquals(before, watchDog);
    }

    @Test
    public void hasDog_sameId_returnsTrue() {
        Dog storedDog = new Dog(3, new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large"));
        watchDog.addDogFromStorage(storedDog);

        Dog sameIdentity = new Dog(3, new DogName("Different Name"),
                new Breed("Poodle"), new Size("Small"));

        assertTrue(watchDog.hasDog(sameIdentity));
    }

    @Test
    public void removeDog_nonexistentDog_throwsIllegalArgumentException() {
        Dog missingDog = new Dog(99, new DogName("Missing"), new Breed("Poodle"), new Size("Small"));

        assertThrows(IllegalArgumentException.class, () -> watchDog.removeDog(missingDog));
        assertTrue(watchDog.getDogList().isEmpty());
    }

    @Test
    public void getRecordLists_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> watchDog.getDogList().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> watchDog.getCustomerList().remove(0));
    }

    @Test
    public void equals_sameRecords_returnsTrue() {
        WatchDog original = getTypicalWatchDog();
        WatchDog copy = new WatchDog(original);

        assertEquals(original, original);
        assertEquals(original, copy);
        assertEquals(copy, original);
        assertEquals(new WatchDog(), new WatchDog());
    }

    @Test
    public void equals_differentDogs_returnsFalse() {
        WatchDog original = getTypicalWatchDog();
        WatchDog differentDogs = new WatchDog(original);
        differentDogs.removeDog(MAX);

        assertEquals(original.getCustomerList(), differentDogs.getCustomerList());
        assertNotEquals(original, differentDogs);
    }

    @Test
    public void equals_differentCustomers_returnsFalse() {
        WatchDog original = getTypicalWatchDog();
        WatchDog differentCustomers = new WatchDog(original);
        differentCustomers.setCustomers(List.of(ALICE));

        assertEquals(original.getDogList(), differentCustomers.getDogList());
        assertNotEquals(original, differentCustomers);
    }

    @Test
    public void equals_differentIdCounters_returnsFalse() {
        WatchDog original = new WatchDog();
        WatchDog differentDogCounter = new WatchDog();
        differentDogCounter.setNextDogId(2);
        WatchDog differentCustomerCounter = new WatchDog();
        differentCustomerCounter.setNextCustomerId(2);
        WatchDog differentActivityCounter = new WatchDog();
        differentActivityCounter.setNextActivityId(2);

        assertNotEquals(original, differentDogCounter);
        assertNotEquals(original, differentCustomerCounter);
        assertNotEquals(original, differentActivityCounter);
    }

    @Test
    public void equals_nullOrDifferentType_returnsFalse() {
        assertFalse(watchDog.equals(null));
        assertFalse(watchDog.equals("WatchDog"));
    }

    @Test
    public void hashCode_equalBooks_returnsSameHashCode() {
        WatchDog original = getTypicalWatchDog();
        WatchDog copy = new WatchDog(original);

        assertEquals(original.hashCode(), original.hashCode());
        assertEquals(original.hashCode(), copy.hashCode());
        assertEquals(new WatchDog().hashCode(), new WatchDog().hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = WatchDog.class.getCanonicalName() + "{dogs=" + watchDog.getDogList()
                + ", customers=" + watchDog.getCustomerList() + "}";
        assertEquals(expected, watchDog.toString());
    }

}
