package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link UnmarkActivityCommandParser}. */
public class UnmarkActivityCommandParserTest {
    private final UnmarkActivityCommandParser parser = new UnmarkActivityCommandParser();

    @Test
    public void parse_invalidArguments_throwsParseException() {
        for (String input : List.of("", " ", "1 2", "-1", "1.0", "abc")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }
}
