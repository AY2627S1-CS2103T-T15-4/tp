package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCustomerCommand;

/** Contains tests for {@link DeleteCustomerCommandParser}. */
public class DeleteCustomerCommandParserTest {

    private static final String INVALID_FORMAT = "Error in deleting customer:\nInvalid format. "
            + "Please use the following format: deletecustomer <id>\nExample: deletecustomer 21";

    private final DeleteCustomerCommandParser parser = new DeleteCustomerCommandParser();

    @Test
    public void parse_validId_returnsDeleteCustomerCommand() {
        assertParseSuccess(parser, "21", new DeleteCustomerCommand(21));
        assertParseSuccess(parser, "  21  ", new DeleteCustomerCommand(21));
        assertParseSuccess(parser, "1", new DeleteCustomerCommand(1));
    }

    @Test
    public void parse_nonPositiveId_throwsParseException() {
        assertParseFailure(parser, "0", INVALID_FORMAT);
        assertParseFailure(parser, "-1", INVALID_FORMAT);
    }

    @Test
    public void parse_nonNumericId_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "abc", INVALID_FORMAT);
        assertParseFailure(parser, "21 extra", INVALID_FORMAT);
    }
}
