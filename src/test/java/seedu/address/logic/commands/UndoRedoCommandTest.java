package seedu.address.logic.commands;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;

public class UndoRedoCommandTest {

    @Test
    public void execute_withoutLogicHistory_throwsCommandException() {
        ModelManager model = new ModelManager();
        assertThrows(CommandException.class, UndoCommand.MESSAGE_NO_COMMAND, () -> new UndoCommand().execute(model));
        assertThrows(CommandException.class, RedoCommand.MESSAGE_NO_COMMAND, () -> new RedoCommand().execute(model));
    }
}
