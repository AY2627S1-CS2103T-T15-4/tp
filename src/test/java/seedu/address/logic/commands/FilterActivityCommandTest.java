package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

public class FilterActivityCommandTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    @Test
    public void execute_combinedFields_filtersWithAndLogic() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new FilterActivityCommand("dog run", DATE, LocalTime.of(16, 0)).execute(model);

        assertEquals("Successfully filtered activities by name: dog run, time: 16:00, date: 2026-10-20",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(0)), model.getFilteredActivityList());
    }

    @Test
    public void execute_dateAndTime_handleRecurrenceAndInclusiveBoundaries() throws Exception {
        ModelManager model = modelWithActivities();

        new FilterActivityCommand(null, DATE.plusDays(7), LocalTime.of(8, 30)).execute(model);

        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
        CommandResult timeResult = new FilterActivityCommand(null, null, LocalTime.of(8, 30)).execute(model);
        assertEquals("Successfully filtered activities by time: 08:30", timeResult.getFeedbackToUser());

        CommandResult dateResult = new FilterActivityCommand(null, DATE, null).execute(model);
        assertEquals("Successfully filtered activities by date: 2026-10-20", dateResult.getFeedbackToUser());

        new FilterActivityCommand(null, DATE, LocalTime.of(16, 30)).execute(model);
        new FilterActivityCommand(null, DATE, LocalTime.of(17, 0)).execute(model);
        assertFalse(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_noMatches_returnsEmptyFilteredList() throws Exception {
        ModelManager model = modelWithActivities();

        new FilterActivityCommand("unknown", null, null).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_nameMatching_isCaseInsensitive() throws Exception {
        ModelManager model = modelWithActivities();

        new FilterActivityCommand("RUN", null, null).execute(model);

        assertEquals(List.of(model.getActivityList().get(0)), model.getFilteredActivityList());
    }

    @Test
    public void execute_recurrenceMatching_isCaseInsensitiveAndIncludedInMessage() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new FilterActivityCommand(null, null, null, Recurrence.DAILY).execute(model);

        assertEquals("Successfully filtered activities by recurrence: Daily", result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
    }

    @Test
    public void execute_combinedRecurrenceAndName_filtersOnlyMatchingActivity() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new FilterActivityCommand("WALK", null, null, Recurrence.DAILY).execute(model);

        assertEquals("Successfully filtered activities by name: WALK, recurrence: Daily",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
    }

    @Test
    public void execute_recurrenceWithNoMatches_returnsEmptyFilteredList() throws Exception {
        ModelManager model = modelWithActivities();

        new FilterActivityCommand("walk", null, null, Recurrence.WEEKLY).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_keepsNewMatchingActivityAfterFilterIsActive() throws Exception {
        ModelManager model = modelWithActivities();
        new FilterActivityCommand("run", null, null).execute(model);
        model.addActivity(new ActivityName("Evening Run"), DATE, LocalTime.of(18, 0),
                LocalTime.of(19, 0), Recurrence.ONCE);

        assertEquals(2, model.getFilteredActivityList().size());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new FilterActivityCommand("run", null, null).execute(null));
    }

    @Test
    public void equals_sameFieldsAndOtherTypes_returnsExpectedResult() {
        FilterActivityCommand command = new FilterActivityCommand("run", DATE, LocalTime.NOON, Recurrence.ONCE);

        assertEquals(command, command);
        assertEquals(command, new FilterActivityCommand("run", DATE, LocalTime.NOON, Recurrence.ONCE));
        assertFalse(command.equals(new FilterActivityCommand("run", DATE, LocalTime.NOON, Recurrence.DAILY)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("command"));
        assertFalse(command.equals(new FilterActivityCommand("other", DATE, LocalTime.NOON, Recurrence.ONCE)));
        assertFalse(command.equals(new FilterActivityCommand("run", DATE.plusDays(1), LocalTime.NOON,
                Recurrence.ONCE)));
        assertFalse(command.equals(new FilterActivityCommand("run", DATE, LocalTime.MIDNIGHT,
                Recurrence.ONCE)));
    }

    private ModelManager modelWithActivities() {
        ModelManager model = new ModelManager();
        model.addActivity(new ActivityName("Dog Run"), DATE, LocalTime.of(16, 0),
                LocalTime.of(17, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Morning Walk"), DATE, LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.DAILY);
        model.addActivity(new ActivityName("Training"), DATE, LocalTime.of(10, 0),
                LocalTime.of(11, 0), Recurrence.WEEKLY);
        return model;
    }
}
