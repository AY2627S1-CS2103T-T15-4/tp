package seedu.address.model.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ActivityTest {
    private static final LocalDate FIRST_DATE = LocalDate.of(2026, 12, 29);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(11, 0);
    private static final ActivityName NAME = new ActivityName("Training");

    private Activity activity(Recurrence recurrence) {
        return new Activity(12, NAME, FIRST_DATE, START, END, recurrence);
    }

    @Test
    public void constructor_validDetails_exposesAllFields() {
        Activity activity = activity(Recurrence.WEEKLY);
        assertEquals(12, activity.getId());
        assertEquals(NAME, activity.getName());
        assertEquals(FIRST_DATE, activity.getStartDate());
        assertEquals(START, activity.getStartTime());
        assertEquals(END, activity.getEndTime());
        assertEquals(Recurrence.WEEKLY, activity.getRecurrence());
        assertEquals(0, new Activity(0, NAME, FIRST_DATE, START, END, Recurrence.ONCE).getId());
    }

    @Test
    public void constructor_invalidDetails_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new Activity(-1, NAME, FIRST_DATE, START, END, Recurrence.ONCE));
        assertThrows(IllegalArgumentException.class, () ->
                new Activity(1, NAME, FIRST_DATE, START, START, Recurrence.ONCE));
        assertThrows(IllegalArgumentException.class, () ->
                new Activity(1, NAME, FIRST_DATE, END, START, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                new Activity(1, null, FIRST_DATE, START, END, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                new Activity(1, NAME, null, START, END, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                new Activity(1, NAME, FIRST_DATE, null, END, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                new Activity(1, NAME, FIRST_DATE, START, null, Recurrence.ONCE));
        assertThrows(NullPointerException.class, () ->
                new Activity(1, NAME, FIRST_DATE, START, END, null));
    }

    @Test
    public void occursOn_recurrenceBoundaries() {
        for (Recurrence recurrence : Recurrence.values()) {
            Activity activity = activity(recurrence);
            assertFalse(activity.occursOn(FIRST_DATE.minusWeeks(1)));
            assertTrue(activity.occursOn(FIRST_DATE));
            assertThrows(NullPointerException.class, () -> activity.occursOn(null));
        }
        assertFalse(activity(Recurrence.ONCE).occursOn(FIRST_DATE.plusDays(1)));
        assertTrue(activity(Recurrence.DAILY).occursOn(LocalDate.of(2027, 1, 1)));
        assertTrue(activity(Recurrence.DAILY).occursOn(LocalDate.of(2028, 2, 29)));
        assertFalse(activity(Recurrence.WEEKLY).occursOn(FIRST_DATE.plusDays(1)));
        assertTrue(activity(Recurrence.WEEKLY).occursOn(LocalDate.of(2027, 1, 5)));
        assertTrue(activity(Recurrence.WEEKLY).occursOn(FIRST_DATE.plusWeeks(10000)));
    }

    @Test
    public void nextOccurrence_inclusiveAndBeforeStart() {
        for (Recurrence recurrence : Recurrence.values()) {
            Activity activity = activity(recurrence);
            assertEquals(Optional.of(FIRST_DATE), activity.nextOccurrenceOnOrAfter(FIRST_DATE.minusDays(1)));
            assertEquals(Optional.of(FIRST_DATE), activity.nextOccurrenceOnOrAfter(FIRST_DATE));
            assertThrows(NullPointerException.class, () -> activity.nextOccurrenceOnOrAfter(null));
        }
        assertEquals(Optional.empty(), activity(Recurrence.ONCE).nextOccurrenceOnOrAfter(FIRST_DATE.plusDays(1)));
        assertEquals(Optional.of(FIRST_DATE.plusDays(1)),
                activity(Recurrence.DAILY).nextOccurrenceOnOrAfter(FIRST_DATE.plusDays(1)));
        assertEquals(Optional.of(LocalDate.of(2027, 1, 5)),
                activity(Recurrence.WEEKLY).nextOccurrenceOnOrAfter(LocalDate.of(2027, 1, 1)));
        assertEquals(Optional.of(FIRST_DATE.plusWeeks(1)),
                activity(Recurrence.WEEKLY).nextOccurrenceOnOrAfter(FIRST_DATE.plusWeeks(1)));
    }

    @Test
    public void nextOccurrence_dateRangeBoundary_doesNotOverflow() {
        Activity weekly = new Activity(1, NAME, LocalDate.MAX.minusDays(1), START, END, Recurrence.WEEKLY);
        assertEquals(Optional.empty(), weekly.nextOccurrenceOnOrAfter(LocalDate.MAX));
        assertEquals(Optional.of(LocalDate.MAX), activity(Recurrence.DAILY).nextOccurrenceOnOrAfter(LocalDate.MAX));
    }

    @Test
    public void replacements_preserveIdentityAndOriginal() {
        Activity original = activity(Recurrence.WEEKLY);
        Activity renamed = original.withName(new ActivityName("Grooming"));
        Activity rescheduled = original.withSchedule(FIRST_DATE.plusDays(1), START.plusHours(1),
                END.plusHours(1), Recurrence.DAILY);
        assertEquals(NAME, original.getName());
        assertEquals(FIRST_DATE, original.getStartDate());
        assertEquals(new ActivityName("Grooming"), renamed.getName());
        assertEquals(original.withName(NAME), original);
        assertEquals(original.getStartDate(), renamed.getStartDate());
        assertEquals(original.getStartTime(), renamed.getStartTime());
        assertEquals(original.getEndTime(), renamed.getEndTime());
        assertEquals(original.getRecurrence(), renamed.getRecurrence());
        assertEquals(FIRST_DATE.plusDays(1), rescheduled.getStartDate());
        assertEquals(START.plusHours(1), rescheduled.getStartTime());
        assertEquals(END.plusHours(1), rescheduled.getEndTime());
        assertEquals(Recurrence.DAILY, rescheduled.getRecurrence());
        assertEquals(NAME, rescheduled.getName());
        assertTrue(original.isSameActivity(renamed));
        assertTrue(original.isSameActivity(rescheduled));
        assertNotEquals(original, renamed);
        assertNotEquals(original, rescheduled);
        assertThrows(NullPointerException.class, () -> original.withName(null));
        assertThrows(IllegalArgumentException.class, () ->
                 original.withSchedule(FIRST_DATE, END, START, Recurrence.DAILY));
    }

    @Test
    public void equality_distinguishesIdentityAndDetails() {
        Activity original = activity(Recurrence.ONCE);
        Activity copy = activity(Recurrence.ONCE);
        assertEquals(original, original);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertTrue(original.isSameActivity(copy));
        assertFalse(original.isSameActivity(null));
        Activity differentId = new Activity(13, NAME, FIRST_DATE, START, END, Recurrence.ONCE);
        assertFalse(original.isSameActivity(differentId));
        assertNotEquals(original, differentId);
        assertNotEquals(original, null);
        assertNotEquals(original, "Training");
        assertNotEquals(original, original.withSchedule(FIRST_DATE.plusDays(1), START, END, Recurrence.ONCE));
        assertNotEquals(original, original.withSchedule(FIRST_DATE, START.minusHours(1), END, Recurrence.ONCE));
        assertNotEquals(original, original.withSchedule(FIRST_DATE, START, END.plusHours(1), Recurrence.ONCE));
        assertNotEquals(original, activity(Recurrence.DAILY));
        assertTrue(original.toString().contains("Training"));
    }

    @Test
    public void completion_markAndUnmark_preserveDetails() {
        Activity original = activity(Recurrence.ONCE);
        Activity completed = original.markCompleted();
        Activity pending = completed.markIncomplete();

        assertTrue(completed.isCompleted());
        assertFalse(pending.isCompleted());
        assertEquals(original.getId(), completed.getId());
        assertEquals(original.getName(), completed.getName());
        assertEquals(original.getStartDate(), completed.getStartDate());
        assertEquals(original.getStartTime(), completed.getStartTime());
        assertEquals(original.getEndTime(), completed.getEndTime());
        assertEquals(original.getRecurrence(), completed.getRecurrence());
    }
}
