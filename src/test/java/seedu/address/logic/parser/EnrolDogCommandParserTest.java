package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EnrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link EnrolDogCommandParser}. */
public class EnrolDogCommandParserTest {
    private final EnrolDogCommandParser parser = new EnrolDogCommandParser();

    @Test
    public void parse_validIds_acceptsCommaSeparatedDogs() throws Exception {
        assertEquals(new EnrolDogCommand(1, List.of(2)), parser.parse(" a/1 d/2 "));
        assertEquals(new EnrolDogCommand(1, List.of(2, 3, 5)), parser.parse("a/1 d/2, 3,5"));
    }

    @Test
    public void parse_invalidInput_rejectsMalformedOrRepeatedIds() {
        for (String input : List.of("", "a/1", "d/2", "a/ d/2", "a/1 d/", "a/1 d/2,2",
                "a/1 d/2,", "a/-1 d/2", "a/1 d/-2", "a/1 d/2.0", "a/x d/2", "a/1 d/x")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }
}
