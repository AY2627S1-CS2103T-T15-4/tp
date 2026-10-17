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
import seedu.address.model.activity.Recurrence;

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
}
