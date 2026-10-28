package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EnrolDogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Contains tests for {@link EnrolDogCommandParser}. */
public class EnrolDogCommandParserTest {
    private final EnrolDogCommandParser parser = new EnrolDogCommandParser();

    @Test
    public void parse_validIds_acceptsCommaSeparatedDogs() throws Exception {
        assertEquals(new EnrolDogCommand(1, LocalDate.of(2028, 2, 29), List.of(2)),
                parser.parse(" a/1 date/2028-02-29 d/2 "));
        assertEquals(new EnrolDogCommand(1, LocalDate.of(2028, 2, 29), List.of(2, 3, 5)),
                parser.parse("a/1 date/2028-02-29 d/2, 3,5"));
    }

    @Test
    public void parse_invalidInput_rejectsMalformedOrRepeatedIds() {
        for (String input : List.of("", "a/1", "d/2", "a/ d/2", "a/1 d/", "a/1 date/2028-02-29 d/2,2",
                "a/1 date/2028-02-29 d/2,", "a/0 date/2028-02-29 d/2", "a/-1 date/2028-02-29 d/2",
                "a/1 date/2028-02-29 d/-2",
                "a/1 date/2028-02-29 d/2.0", "a/x date/2028-02-29 d/2", "a/1 date/2028-02-29 d/x",
                "a/1 date/2028-02-30 d/2", "a/999999999999 date/2028-02-29 d/2",
                "a/1 date/2028-02-29 d/", "a/-1 date/2028-02-29 d/2")) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void parse_malformedDatedInput_reportsExpectedFormat() {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse("a/1 d/2"));

        assertEquals("Invalid enrol format. Use: enrol a/ACTIVITY_ID date/YYYY-MM-DD "
                + "d/DOG_ID[,DOG_ID...]. Example: enrol a/1 date/2026-10-27 d/2,3", exception.getMessage());
    }

    @Test
    public void parse_invalidDate_returnsSpecificMessage() {
        assertEquals("Date must be a real calendar date in YYYY-MM-DD format.",
                parseException("a/1 date/2028-02-30 d/2").getMessage());
    }

    private ParseException parseException(String input) {
        return org.junit.jupiter.api.Assertions.assertThrows(ParseException.class, () -> parser.parse(input));
    }
}
