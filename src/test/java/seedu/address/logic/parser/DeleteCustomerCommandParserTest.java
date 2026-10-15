package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCustomerCommand;

/** Contains tests for {@link DeleteCustomerCommandParser}. */
public class DeleteCustomerCommandParserTest {

    private static final String INVALID_FORMAT = "Error in deleting customer:\nInvalid format. "
            + "Please use the following format: delete <category> <id>\nExample: delete customer 21";

    private final DeleteCustomerCommandParser parser = new DeleteCustomerCommandParser();

    @Test
    public void parse_validId_returnsDeleteCustomerCommand() {
        assertParseSuccess(parser, "21", new DeleteCustomerCommand(21));
        assertParseSuccess(parser, "  21  ", new DeleteCustomerCommand(21));
        assertParseSuccess(parser, "0", new DeleteCustomerCommand(0));
    }

    @Test
    public void parse_negativeId_throwsParseException() {
        assertParseFailure(parser, "-1", INVALID_FORMAT);
    }

    @Test
    public void parse_nonNumericId_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "abc", INVALID_FORMAT);
        assertParseFailure(parser, "21 extra", INVALID_FORMAT);
    }
}
