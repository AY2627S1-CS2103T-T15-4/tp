//@@author Kenneth-Chia-unused
// Retired global search in v1.5 in favour of field-based searchc/searchd/searcha.
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SearchCommandParserTest {
    private final WatchDogParser parser = new WatchDogParser();

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
        assertEquals(new SearchDogCommand("Buddy", null, null, null),
                parser.parseCommand("search dog n/Buddy"));
        assertEquals(new SearchCustomerCommand("Alex", null, null, null, null),
                parser.parseCommand("search customer n/Alex"));
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

    @Test
    public void parse_blankNameValue_rejectedBySearchParser() {
        ParseException exception = assertThrows(ParseException.class, () ->
                new SearchCommandParser().parse("n/   "));
        assertThrows(ParseException.class, () ->
                new SearchCommandParser().parse("n/\u2003"));
        assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                SearchCommand.MESSAGE_USAGE), exception.getMessage());
    }
}
