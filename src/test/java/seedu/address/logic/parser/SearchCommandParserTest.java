package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SearchCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validInput_createsExpectedCommand() throws Exception {
        assertEquals(new SearchCommand("Little Bud"),
                parser.parseCommand("  search\t n/ Little Bud  "));
        assertEquals(new SearchCommand(0),
                parser.parseCommand("search id/0"));
        assertEquals(new SearchCommand(12),
                parser.parseCommand("search id/0012"));
        assertEquals(new SearchCommand(Integer.MAX_VALUE),
                parser.parseCommand("search id/2147483647"));
    }

    @Test
    public void parse_invalidInput_throwsParseException() {
        String[] invalidInputs = {"search", "search rabbit n/Alex", "search Buddy",
            "search n/", "search n/   ", "search id/", "search id/-1",
            "search id/+1",
            "search id/1.5", "search id/2147483648", "search id/one",
            "search id/1 2",
            "search n/Buddy id/0", "search id/0 n/Buddy", "search n/Buddy n/Max",
            "search id/0 id/1", "search b/Poodle", "search n/Buddy b/Poodle",
            "search extra n/Buddy"};
        for (String input : invalidInputs) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand(input), input);
            assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    SearchCommand.MESSAGE_USAGE), exception.getMessage());
        }
    }
}
