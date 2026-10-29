// @@author XanderGoh
package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteDogCommand;
import seedu.address.testutil.Assert;

/** Contains tests for {@link DeleteDogCommandParser}. */
public class DeleteDogCommandParserTest {

    private final DeleteDogCommandParser parser = new DeleteDogCommandParser();

    @Test
    public void parse_validId_returnsDeleteDogCommand() {
        assertParseSuccess(parser, "12", new DeleteDogCommand(12));
    }

    @Test
    public void parse_negativeId_throwsParseException() {
        assertParseFailure(parser, "-1", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: deletedog <id>\nExample: deletedog 12");
    }

    @Test
    public void parse_zeroId_throwsParseException() {
        assertParseFailure(parser, "0", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: deletedog <id>\nExample: deletedog 12");
    }

    @Test
    public void command_zeroId_rejectsInvalidDogId() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new DeleteDogCommand(0));
    }

    @Test
    public void parse_nonNumericId_throwsParseException() {
        assertParseFailure(parser, "abc", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: deletedog <id>\nExample: deletedog 12");
    }

    @Test
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, "   ", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: deletedog <id>\nExample: deletedog 12");
    }
}
