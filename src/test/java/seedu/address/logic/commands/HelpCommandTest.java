package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE, true, false);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HelpCommand().execute(null));
    }

    @Test
    public void constructor_blankHelp_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HelpCommand("  "));
    }

    @Test
    public void execute_commandHelp_displaysFeedbackWithoutOpeningWindow() {
        String help = "Format: summary\nExample: summary\nSpecifications: Accepts no parameters.";
        assertCommandSuccess(new HelpCommand(help), model, new CommandResult(help), expectedModel);
    }
}
