package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SelectCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link SelectCustomerCommandParser}. */
public class SelectCustomerCommandParserTest {

    private final AddressBookParser addressBookParser = new AddressBookParser();
    private final SelectCustomerCommandParser parser = new SelectCustomerCommandParser();

    @Test
    public void parse_validId_returnsSelectCustomerCommand() {
        assertParseSuccess(parser, " customer 21", new SelectCustomerCommand(21));
        assertParseSuccess(parser, "   customer   1  ", new SelectCustomerCommand(1));
        assertParseSuccess(parser, "customer 021", new SelectCustomerCommand(21));
    }

    @Test
    public void parse_invalidFormat_throwsParseException() {
        assertParseFailure(parser, "   ", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " Customer 21", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer 0", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer -1", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer abc", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer 21 extra", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " customer 2147483648", SelectCustomerCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownCategory_throwsInvalidFormat() {
        assertParseFailure(new SelectCommandParser(), " activity 1", SelectCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_wrongCommandCase_throwsUnknownCommand() {
        assertEquals(MESSAGE_UNKNOWN_COMMAND,
                assertThrows(ParseException.class, () -> addressBookParser.parseCommand("Select customer 21"))
                        .getMessage());
    }
}
