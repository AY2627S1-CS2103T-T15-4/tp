package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link EditDogCommandParser}. */
public class EditDogCommandParserTest {
    private final EditDogCommandParser parser = new EditDogCommandParser();

    @Test
    public void parse_validFields_returnsCommand() throws Exception {
        EditDogCommand command = parser.parse("1 n/Milo b/Beagle s/Small");
        assertNotNull(command);
    }

    @Test
    public void parse_invalidId_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("abc n/Milo"));
        assertThrows(ParseException.class, () -> parser.parse("-1 n/Milo"));
        assertThrows(ParseException.class, () -> parser.parse(" n/Milo"));
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("1 n/Milo n/Cookie"));
    }

    @Test
    public void parse_noEditedFields_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("1"));
    }
}
