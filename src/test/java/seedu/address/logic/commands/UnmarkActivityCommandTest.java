package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Contains tests for {@link UnmarkActivityCommand}. */
public class UnmarkActivityCommandTest {
    @Test
    public void execute_existingActivity_marksItPending() throws Exception {
        ModelManager model = new ModelManager();
        model.addActivity(new ActivityName("Walk"), LocalDate.of(2026, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.markActivity(1);

        new UnmarkActivityCommand(1).execute(model);

        assertFalse(model.getActivityList().get(0).isCompleted());
    }

    @Test
    public void execute_missingActivity_throwsCommandException() {
        assertThrows(CommandException.class, () -> new UnmarkActivityCommand(1).execute(new ModelManager()));
    }

    @Test
    public void constructor_negativeId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new UnmarkActivityCommand(-1));
    }
}
