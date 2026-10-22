package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterUnlinkedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class FilterUnlinkedCommandParserTest {
    private final FilterUnlinkedCommandParser parser = new FilterUnlinkedCommandParser();

    @Test
    public void parse_validCategories_acceptsWhitespace() throws Exception {
        assertEquals(new FilterUnlinkedCommand(true), parser.parse("  dog  "));
        assertEquals(new FilterUnlinkedCommand(false), parser.parse(" customer "));
    }

    @Test
    public void parse_invalidCategory_throwsParseException() {
        for (String input : new String[]{"", "Dog", "customer extra", "cat"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }
}
