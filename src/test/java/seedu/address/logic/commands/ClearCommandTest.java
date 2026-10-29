package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalWatchDog.getTypicalWatchDog;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;

public class ClearCommandTest {

    @Test
    public void execute_emptyWatchDog_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyWatchDog_success() {
        Model model = new ModelManager(getTypicalWatchDog(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalWatchDog(), new UserPrefs());
        expectedModel.setWatchDog(new WatchDog());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

}
