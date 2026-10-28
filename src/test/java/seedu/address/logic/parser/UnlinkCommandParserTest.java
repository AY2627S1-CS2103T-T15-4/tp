package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class UnlinkCommandParserTest {
    private final UnlinkCommandParser parser = new UnlinkCommandParser();

    @Test
    public void parse_validIds_acceptsSingleAndMultipleDogs() throws Exception {
        assertEquals(new UnlinkCommand(List.of(2)), parser.parse(" d/2 "));
        assertEquals(new UnlinkCommand(List.of(1, 2, Integer.MAX_VALUE)),
                parser.parse("\t d/01  d/02\t d/2147483647  "));
        assertEquals(new UnlinkCommand(List.of(2, 3)),
                new AddressBookParser().parseCommand("unlink d/2 d/3"));
        assertEquals(new UnlinkCommand(1), new AddressBookParser().parseCommand("unlink c/1"));
        assertEquals(new UnlinkCommand(0), parser.parse(" c/00 "));
        assertEquals(new UnlinkCommand(Integer.MAX_VALUE), parser.parse("c/2147483647"));
    }

    @Test
    public void parse_invalidInput_rejectsMissingUnknownRepeatedAndMalformedFields() {
        for (String input : List.of("", " ", "c/", "d/", "d/2 d/2", "d/2 d/02", "c/1 c/2",
                "c/1 d/2", "d/2 c/1", "d/2 x/3", "prefix d/2", "d/2 extra", "C/1", "D/2",
                "d/1d/2", "c/ 1", "d/ 2", "c/-1", "d/-2", "c/+1", "d/+2", "d/2.0", "c/1.0",
                "c/a", "d/a", "d/0", "c/2147483648", "d/2147483648", "d/999999999999999999999")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void command_zeroDogId_rejectsInvalidDogId() {
        assertThrows(IllegalArgumentException.class, () -> new UnlinkCommand(List.of(0)));
    }
}
