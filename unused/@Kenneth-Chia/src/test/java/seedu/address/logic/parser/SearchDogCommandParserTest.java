package seedu.address.logic.parser;
//@@author Kenneth-Chia-unused
// Original category-search parser tests, superseded by searchd tests in PR #150;
// retained for authorship and reference.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.dog.DogSearchPredicate;

public class SearchDogCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validInput_createsExpectedCommand() throws Exception {
        assertEquals(new SearchDogCommand(new DogSearchPredicate("Little Bud")),
                parser.parseCommand("  search\tdog\t n/ Little Bud  "));
        assertEquals(new SearchDogCommand(new DogSearchPredicate(0)),
                parser.parseCommand("search dog id/0"));
        assertEquals(new SearchDogCommand(new DogSearchPredicate(12)),
                parser.parseCommand("search dog id/0012"));
        assertEquals(new SearchDogCommand(new DogSearchPredicate(Integer.MAX_VALUE)),
                parser.parseCommand("search dog id/2147483647"));
    }

    @Test
    public void parse_invalidInput_throwsParseException() {
        String[] invalidInputs = {"search dog", "search dog Buddy",
            "search dog n/", "search dog n/   ", "search dog id/", "search dog id/-1", "search dog id/+1",
            "search dog id/1.5", "search dog id/2147483648", "search dog id/one", "search dog id/1 2",
            "search dog n/Buddy id/0", "search dog id/0 n/Buddy", "search dog n/Buddy n/Max",
            "search dog id/0 id/1", "search dog b/Poodle", "search dog n/Buddy b/Poodle",
            "search dog extra n/Buddy"};
        for (String input : invalidInputs) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand(input), input);
            assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    SearchDogCommand.MESSAGE_USAGE), exception.getMessage());
        }
    }

    @Test
    public void parse_blankNameValue_rejectedByDogParser() {
        ParseException exception = assertThrows(ParseException.class, () ->
                new SearchDogCommandParser().parse("dog n/   "));
        assertThrows(ParseException.class, () ->
                new SearchDogCommandParser().parse("dog n/\u2003"));
        assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                SearchDogCommand.MESSAGE_USAGE), exception.getMessage());
    }
}
