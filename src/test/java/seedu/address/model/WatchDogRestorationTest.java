// @@author Kenneth-Chia
package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Exercises validation of incoming snapshots independently of JSON loading. */
public class WatchDogRestorationTest {
    private static final LocalDate DATE = LocalDate.of(2000, 2, 29);
    private static final Dog DOG = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    private static final Activity FIRST = activity(1, 9, 10);
    private static final Activity OVERLAPPING = activity(2, 9, 11);
    private static final Activity ADJACENT = activity(3, 10, 11);

    @Test
    public void addActivity_listenerResetsBook_detectsInvalidatedAllocation() {
        WatchDog target = new WatchDog();
        AtomicBoolean resetPending = new AtomicBoolean(true);
        target.getActivityList().addListener((ListChangeListener<Activity>) change -> {
            if (resetPending.getAndSet(false)) {
                target.resetData(new WatchDog());
            }
        });

        assertThrows(AssertionError.class, () -> target.addActivity(new ActivityName("Walk"), DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE));
    }

    @Test
    public void resetData_listenerRemovesRestoredActivity_detectsInvalidatedSnapshot() {
        WatchDog target = new WatchDog();
        AtomicBoolean removalPending = new AtomicBoolean(true);
        target.getActivityList().addListener((ListChangeListener<Activity>) change -> {
            if (removalPending.getAndSet(false)) {
                target.removeActivity(FIRST);
            }
        });

        assertThrows(AssertionError.class, () -> target.resetData(snapshot(List.of())));
    }

    @Test
    public void resetData_listenerRemovesRestoredBooking_detectsInvalidatedSnapshot() {
        WatchDog target = new WatchDog();
        AtomicBoolean removalPending = new AtomicBoolean(true);
        target.getActivityList().addListener((ListChangeListener<Activity>) change -> {
            if (removalPending.getAndSet(false)) {
                assertDoesNotThrow(() -> target.unenrolDogs(1, DATE, List.of(7)));
            }
        });

        assertThrows(AssertionError.class, () -> target.resetData(snapshot(List.of(new Enrolment(1, 7, DATE)))));
    }

    @Test
    public void resetData_invalidEnrolments_rejectsBeforePublishingAnyChanges() {
        Enrolment valid = new Enrolment(1, 7, DATE);
        List<List<Enrolment>> invalidBatches = List.of(
                List.of(valid, new Enrolment(99, 7, DATE)),
                List.of(valid, new Enrolment(1, 99, DATE)),
                List.of(valid, valid),
                List.of(valid, new Enrolment(1, 7, DATE.plusDays(1))),
                List.of(valid, new Enrolment(2, 7, DATE)));
        for (List<Enrolment> batch : invalidBatches) {
            WatchDog target = new WatchDog();
            target.addActivity(new ActivityName("Existing"), DATE, LocalTime.NOON,
                    LocalTime.of(13, 0), Recurrence.DAILY);
            WatchDog before = new WatchDog(target);
            AtomicInteger changes = new AtomicInteger();
            target.getActivityList().addListener((ListChangeListener<Activity>) change -> changes.incrementAndGet());
            target.getDogList().addListener((ListChangeListener<Dog>) change -> changes.incrementAndGet());
            target.getEnrolmentList().addListener((ListChangeListener<Enrolment>) change -> changes.incrementAndGet());

            assertThrows(IllegalArgumentException.class, () -> target.resetData(snapshot(batch)));

            assertEquals(before, target);
            assertEquals(0, changes.get());
        }
    }

    @Test
    public void resetData_validHistoricalAndAdjacentBookings_preservesSnapshotAndLiveViews() {
        List<Enrolment> bookings = List.of(new Enrolment(1, 7, DATE), new Enrolment(3, 7, DATE));
        WatchDog target = new WatchDog();
        ObservableList<Activity> activityView = target.getActivityList();
        ObservableList<Enrolment> enrolmentView = target.getEnrolmentList();

        target.resetData(snapshot(bookings));

        assertEquals(List.of(FIRST, OVERLAPPING, ADJACENT), activityView);
        assertEquals(bookings, enrolmentView);
        assertEquals(List.of(DOG), target.getDogList());
        assertEquals(4, target.getNextActivityId());
        assertEquals(target, new WatchDog(target));
        target.resetData(target);
        assertEquals(bookings, enrolmentView);
    }

    private static ReadOnlyWatchDog snapshot(List<Enrolment> bookings) {
        return new ReadOnlyWatchDog() {
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.emptyObservableList();
            }

            @Override
            public ObservableList<Dog> getDogList() {
                return FXCollections.observableArrayList(DOG);
            }

            @Override
            public ObservableList<Activity> getActivityList() {
                return FXCollections.observableArrayList(FIRST, OVERLAPPING, ADJACENT);
            }

            @Override
            public ObservableList<Enrolment> getEnrolmentList() {
                return FXCollections.observableArrayList(bookings);
            }

            @Override
            public long getNextActivityId() {
                return 4;
            }
        };
    }

    private static Activity activity(int id, int startHour, int endHour) {
        return new Activity(id, new ActivityName("Walk"), DATE, LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0), Recurrence.ONCE);
    }
}
