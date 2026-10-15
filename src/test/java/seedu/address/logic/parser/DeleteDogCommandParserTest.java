package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteDogCommand;

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
                + "Please use the following format: delete <category> <id>\nExample: delete dog 12");
    }

    @Test
    public void parse_nonNumericId_throwsParseException() {
        assertParseFailure(parser, "abc", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: delete <category> <id>\nExample: delete dog 12");
    }

    @Test
    public void parse_missingId_throwsParseException() {
        assertParseFailure(parser, "   ", "Error in deleting dog:\nInvalid format. "
                + "Please use the following format: delete <category> <id>\nExample: delete dog 12");
    }
}
