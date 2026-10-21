package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SelectDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link SelectDogCommandParser}. */
public class SelectDogCommandParserTest {

    private final AddressBookParser addressBookParser = new AddressBookParser();
    private final SelectDogCommandParser parser = new SelectDogCommandParser();

    @Test
    public void parse_validId_returnsSelectDogCommand() {
        assertParseSuccess(parser, " dog 21", new SelectDogCommand(21));
        assertParseSuccess(parser, "   dog   0  ", new SelectDogCommand(0));
        assertParseSuccess(parser, "dog 021", new SelectDogCommand(21));
    }

    @Test
    public void parse_invalidFormat_throwsParseException() {
        assertParseFailure(parser, "   ", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dog", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " Dog 21", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dog -1", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dog abc", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dog 21 extra", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dog 2147483648", SelectDogCommandParser.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_wrongCommandCase_throwsUnknownCommand() {
        assertThrows(ParseException.class, () -> addressBookParser.parseCommand("Select dog 21"));
        assertEquals(MESSAGE_UNKNOWN_COMMAND,
                assertThrows(ParseException.class, () -> addressBookParser.parseCommand("Select dog 21")).getMessage());
    }
}
