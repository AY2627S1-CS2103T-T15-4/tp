package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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

public class ActivityIntegrationTest {
    private static final ActivityName NAME = new ActivityName("Walk");
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(10, 0);

    @Test
    public void addActivity_observableReadOnlyViewAndCopies_preserveIds() {
        ModelManager model = new ModelManager();
        ObservableList<Activity> view = model.getActivityList();
        AtomicInteger changes = new AtomicInteger();
        view.addListener((ListChangeListener<Activity>) change -> changes.incrementAndGet());
        Activity first = model.addActivity(NAME, DATE, START, END, Recurrence.DAILY);
        assertEquals(List.of(first), view);
        assertEquals(1, changes.get());
        assertThrows(UnsupportedOperationException.class, () -> view.add(first));
        AddressBook copy = new AddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), copy);
        assertEquals(model.getAddressBook().hashCode(), copy.hashCode());
        assertEquals(2, copy.addActivity(NAME, DATE, START, END, Recurrence.DAILY).getId());
        assertFalse(copy.equals(model.getAddressBook()));
        model.setAddressBook(copy);
        assertEquals(copy.getActivityList(), view);
        assertEquals(3, model.addActivity(NAME, DATE, START, END, Recurrence.ONCE).getId());
    }

    @Test
    public void addActivity_invalidDetailsAndDuplicateIdentity_leaveStateUnchanged() {
        AddressBook book = new AddressBook();
        assertThrows(IllegalArgumentException.class, () ->
                book.addActivity(NAME, DATE, END, START, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                book.addActivity(null, DATE, START, END, Recurrence.ONCE));
        assertEquals(1, book.getNextActivityId());
        assertTrue(book.getActivityList().isEmpty());
        Activity activity = book.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        AddressBook before = new AddressBook(book);
        assertThrows(IllegalArgumentException.class, () ->
                book.addActivityFromStorage(activity.withName(new ActivityName("Changed"))));
        assertThrows(NullPointerException.class, () -> book.addActivityFromStorage(null));
        for (long counter : new long[]{-1, 0, 1, (long) Integer.MAX_VALUE + 2}) {
            assertThrows(IllegalArgumentException.class, () -> book.setNextActivityId(counter));
        }
        assertEquals(before, book);
    }

    @Test
    public void addActivity_lastAvailableId_doesNotOverflow() {
        AddressBook book = new AddressBook();
        book.setNextActivityId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, book.addActivity(NAME, DATE, START, END, Recurrence.ONCE).getId());
        assertEquals((long) Integer.MAX_VALUE + 1, book.getNextActivityId());
        AddressBook copy = new AddressBook(book);
        assertThrows(IllegalStateException.class, () ->
                book.addActivity(NAME, DATE, START, END, Recurrence.ONCE));
        assertEquals(copy, book);
        AddressBook differentCounter = new AddressBook();
        differentCounter.setNextActivityId(2);
        assertFalse(new AddressBook().equals(differentCounter));
    }

    @Test
    public void resetData_invalidActivityIdentityOrCounter_leavesBookUnchanged() {
        AddressBook book = new AddressBook();
        Activity stored = book.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        AddressBook before = new AddressBook(book);
        AddressBook duplicateIds = new AddressBook() {
            @Override
            public ObservableList<Activity> getActivityList() {
                return FXCollections.observableArrayList(stored, stored);
            }
        };
        assertThrows(IllegalArgumentException.class, () -> book.resetData(duplicateIds));
        for (long counter : new long[]{0, (long) Integer.MAX_VALUE + 2}) {
            AddressBook invalidCounter = new AddressBook() {
                @Override
                public long getNextActivityId() {
                    return counter;
                }
            };
            assertThrows(IllegalArgumentException.class, () -> book.resetData(invalidCounter));
        }
        assertEquals(before, book);
    }

    @Test
    public void resetData_listenerCopiesBook_observesRestoredCounter() {
        AddressBook target = new AddressBook();
        AddressBook source = new AddressBook();
        source.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        AtomicReference<AddressBook> snapshot = new AtomicReference<>();
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        target.getActivityList().addListener((ListChangeListener<Activity>) change -> {
            try {
                snapshot.set(new AddressBook(target));
            } catch (RuntimeException e) {
                failure.set(e);
            }
        });

        target.resetData(source);

        assertNull(failure.get());
        assertEquals(source, snapshot.get());
        assertEquals(2, snapshot.get().getNextActivityId());
    }

    @Test
    public void resetData_activityAndEnrolmentListeners_observeCompleteRestoredState() throws Exception {
        AddressBook target = new AddressBook();
        Activity oldActivity = target.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        Dog oldDog = target.addDog(new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("small")));
        target.enrolDogs(oldActivity.getId(), List.of(oldDog.getId()));
        AddressBook source = new AddressBook();
        source.setNextActivityId(5);
        Activity newActivity = source.addActivity(NAME, DATE, START, END, Recurrence.WEEKLY);
        Dog newDog = source.addDog(new Dog(new DogName("Biscuit"), new Breed("Poodle"), new Size("small")));
        source.enrolDogs(newActivity.getId(), List.of(newDog.getId()));
        source.setNextDogId(20);
        source.setNextCustomerId(30);
        AtomicReference<AddressBook> activitySnapshot = new AtomicReference<>();
        AtomicReference<AddressBook> enrolmentSnapshot = new AtomicReference<>();
        target.getActivityList().addListener((ListChangeListener<Activity>) change ->
                activitySnapshot.set(new AddressBook(target)));
        target.getEnrolmentList().addListener((ListChangeListener<Enrolment>) change ->
                enrolmentSnapshot.set(new AddressBook(target)));

        target.resetData(source);

        assertEquals(source, activitySnapshot.get());
        assertEquals(source, enrolmentSnapshot.get());
        assertEquals(20, activitySnapshot.get().getNextDogId());
        assertEquals(30, activitySnapshot.get().getNextCustomerId());
        assertEquals(20, enrolmentSnapshot.get().getNextDogId());
        assertEquals(30, enrolmentSnapshot.get().getNextCustomerId());
        assertEquals(source, target);
    }

    @Test
    public void equals_differentSchedulesWithSameCounter_notEqual() {
        AddressBook first = new AddressBook();
        AddressBook second = new AddressBook();
        first.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        second.addActivity(new ActivityName("Training"), DATE, START, END, Recurrence.ONCE);

        assertEquals(first.getNextActivityId(), second.getNextActivityId());
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
        assertEquals(first, new AddressBook(first));
    }

    @Test
    public void equals_differentCustomersWithSameActivities_notEqual() {
        AddressBook first = new AddressBook();
        first.addActivity(NAME, DATE, START, END, Recurrence.ONCE);
        AddressBook second = new AddressBook(first);
        second.addCustomer(new Customer(new Name("Alice Tan"), new Phone("91234567"),
                new Email("alice@example.com"), new BackupContact("98765432")));

        assertEquals(first.getActivityList(), second.getActivityList());
        assertEquals(first.getNextActivityId(), second.getNextActivityId());
        assertFalse(first.equals(second));
        assertFalse(second.equals(first));
    }
}
