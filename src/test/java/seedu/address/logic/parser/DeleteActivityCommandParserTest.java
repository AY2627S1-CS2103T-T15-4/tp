package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Tests parsing a persistent ID for {@code deleteactivity}. */
public class DeleteActivityCommandParserTest {
    private final DeleteActivityCommandParser parser = new DeleteActivityCommandParser();

    @Test
    public void parse_validId_returnsCommand() throws Exception {
        assertEquals(new DeleteActivityCommand(21), parser.parse(" 21 "));
        assertEquals(new DeleteActivityCommand(1), new WatchDogParser().parseCommand("deleteactivity 1"));
    }

    @Test
    public void parse_missingNegativeOverflowOrExtraText_rejected() {
        for (String input : new String[]{"", "0", "-1", "2147483648", "1 2", "1 n/Walk", "one"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }
}
