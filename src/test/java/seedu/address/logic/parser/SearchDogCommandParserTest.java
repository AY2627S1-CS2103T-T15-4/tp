// @@author XanderGoh
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SearchDogCommandParserTest {
    private final SearchDogCommandParser parser = new SearchDogCommandParser();

    @Test
    public void parse_validFields_normalizesValues() throws Exception {
        SearchDogCommand expected = new SearchDogCommand("Waffle Boy", "Golden Retriever", "medium", 12);

        assertEquals(expected, parser.parse(" n/Waffle   Boy b/Golden   Retriever s/MEDIUM i/12 "));
    }

    @Test
    public void parse_eachSupportedField_acceptsValidValue() throws Exception {
        assertEquals(new SearchDogCommand("Waffle", null, null, null), parser.parse("n/Waffle"));
        assertEquals(new SearchDogCommand(null, "Golden Retriever", null, null),
                parser.parse("b/Golden Retriever"));
        assertEquals(new SearchDogCommand(null, null, "large", null), parser.parse("s/LARGE"));
        assertEquals(new SearchDogCommand(null, null, null, 21), parser.parse("i/21"));
    }

    @Test
    public void parse_invalidFields_throwsParseException() {
        String[] invalidInputs = {"", "n/Waffle n/Max", "b/Poodle b/Lab", "s/tiny", "i/0", "i/-1", "i/abc",
            "N/Waffle", "x/value", "n/Lala x/value", "n/", "s/medium s/large", "i/1 i/2",
            "text n/Waffle"};

        for (String invalidInput : invalidInputs) {
            assertThrows(ParseException.class, () -> parser.parse(invalidInput));
        }
    }

    @Test
    public void parse_eachInvalidField_rejectsIndividually() {
        assertThrows(ParseException.class, () -> parser.parse("n/"));
        assertThrows(ParseException.class, () -> parser.parse("b/"));
        assertThrows(ParseException.class, () -> parser.parse("s/tiny"));
        assertThrows(ParseException.class, () -> parser.parse("i/not-a-number"));
    }

    @Test
    public void parse_invalidId_includesSearchingErrorMessage() {
        for (String value : new String[]{"-1", "0", "00", "not-a-number", "2147483648", "-2147483649"}) {
            assertThrows(ParseException.class, "Error in searching dogs:\nPlease enter a valid dog ID", () ->
                    parser.parse("i/" + value));
        }
    }

    @Test
    public void parse_invalidSize_includesSearchingErrorMessage() {
        String expectedMessage = "Error in searching dogs:\n"
                + "Invalid dog size. Please input small/medium/large/giant only.";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse("s/tiny"));
    }

    @Test
    public void command_zeroId_rejectsInvalidDogId() {
        assertThrows(IllegalArgumentException.class, () -> new SearchDogCommand(null, null, null, 0));
    }
}
