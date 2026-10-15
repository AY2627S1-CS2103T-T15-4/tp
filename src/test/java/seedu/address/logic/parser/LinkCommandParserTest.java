package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LinkCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class LinkCommandParserTest {
    private final LinkCommandParser parser = new LinkCommandParser();

    @Test
    public void parse_validIds_acceptsSingleAndMultipleDogs() throws Exception {
        assertEquals(new LinkCommand(1, List.of(2)), parser.parse(" c/1 d/2 "));
        assertEquals(new LinkCommand(0, List.of(2, Integer.MAX_VALUE)),
                parser.parse("\t c/0  d/02\t d/2147483647  "));
        assertEquals(new LinkCommand(1, List.of(2, 3)),
                new AddressBookParser().parseCommand("link c/1 d/2 d/3"));
    }

    @Test
    public void parse_invalidInput_rejectsMissingUnknownRepeatedAndMalformedFields() {
        for (String input : List.of("", " ", "c/1", "d/2", "c/ d/2", "c/1 d/", "c/1 d/2 d/2",
                "c/1 d/2 d/02", "c/1 c/2 d/3", "c/1 d/2 x/3", "prefix c/1 d/2", "c/1 d/2 extra",
                "d/2 c/1", "C/1 d/2", "c/1 D/2", "c/1d/2", "c/ 1 d/2", "c/1 d/ 2",
                "c/-1 d/2", "c/1 d/-2", "c/+1 d/2", "c/1 d/2.0", "c/a d/2", "c/1 d/a",
                "c/2147483648 d/2", "c/1 d/2147483648", "c/1 d/999999999999999999999")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }
}
