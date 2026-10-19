package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Contains tests for {@link MarkActivityCommand}. */
public class MarkActivityCommandTest {
    @Test
    public void execute_existingActivity_marksItDone() throws Exception {
        ModelManager model = new ModelManager();
        model.addActivity(new ActivityName("Walk"), LocalDate.of(2026, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);

        CommandResult result = new MarkActivityCommand(1).execute(model);

        assertEquals("Activity 1 marked as done.", result.getFeedbackToUser());
        assertEquals(true, model.getActivityList().get(0).isCompleted());
    }

    @Test
    public void execute_missingActivity_throwsCommandException() {
        assertThrows(CommandException.class, () -> new MarkActivityCommand(1).execute(new ModelManager()));
    }

    @Test
    public void constructor_negativeId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new MarkActivityCommand(-1));
    }
}
