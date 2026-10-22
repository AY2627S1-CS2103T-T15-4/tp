package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class FilterDogCommandParserTest {
    private final FilterDogCommandParser parser = new FilterDogCommandParser();

    @Test
    public void parse_validFields_normalizesValues() throws Exception {
        FilterDogCommand expected = new FilterDogCommand("Waffle Boy", "Golden Retriever", "medium", 12);

        assertEquals(expected, parser.parse(" n/Waffle   Boy b/Golden   Retriever s/MEDIUM i/12 "));
    }

    @Test
    public void parse_invalidFields_throwsParseException() {
        String[] invalidInputs = {"", "n/Waffle n/Max", "b/Poodle b/Lab", "s/tiny", "i/-1", "i/abc",
            "N/Waffle", "x/value", "n/Lala x/value", "n/", "s/medium s/large", "i/1 i/2",
            "text n/Waffle"};

        for (String invalidInput : invalidInputs) {
            assertThrows(ParseException.class, () -> parser.parse(invalidInput));
        }
    }
}
