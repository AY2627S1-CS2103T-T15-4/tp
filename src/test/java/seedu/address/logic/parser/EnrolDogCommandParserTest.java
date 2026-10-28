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
    public void parse_validFields_acceptsAnyOrderAndWhitespaceSeparatedDogs() throws Exception {
        EnrolDogCommand expected = new EnrolDogCommand(1, LocalDate.of(2028, 2, 29), List.of(2, 3, 5));

        assertEquals(new EnrolDogCommand(1, LocalDate.of(2028, 2, 29), List.of(2)),
                parser.parse(" a/1 date/2028-02-29 d/2 "));
        assertEquals(expected, parser.parse("a/1 date/2028-02-29 d/2  3\t5"));
        assertEquals(expected, parser.parse("d/2 3 5 a/1 date/2028-02-29"));
        assertEquals(expected, parser.parse("date/2028-02-29 d/2 3 5 a/1"));
    }

    @Test
    public void parse_missingOrEmptyActivityId_returnsSpecificMessageWithGuidance() {
        String expected = expected("Please enter an activity ID.");

        assertMessage("", expected);
        assertMessage("date/2028-02-29 d/2", expected);
        assertMessage("a/ date/2028-02-29 d/2", expected);
    }

    @Test
    public void parse_missingOrEmptyDate_returnsSpecificMessageWithGuidance() {
        String expected = expected("Please enter a date.");

        assertMessage("a/1 d/2", expected);
        assertMessage("a/1 date/ d/2", expected);
    }

    @Test
    public void parse_missingOrEmptyDogIds_returnsSpecificMessageWithGuidance() {
        String expected = expected("Please enter at least one dog ID.");

        assertMessage("a/1 date/2028-02-29", expected);
        assertMessage("a/1 date/2028-02-29 d/", expected);
    }

    @Test
    public void parse_invalidActivityId_returnsSpecificMessageWithGuidance() {
        String expected = expected("Please provide a positive, valid activity ID.");

        for (String activityId : List.of("0", "-1", "x", "1.5", "999999999999")) {
            assertMessage("a/" + activityId + " date/2028-02-29 d/2", expected);
        }
    }

    @Test
    public void parse_invalidDate_returnsSpecificMessageWithGuidance() {
        String expected = expected("Date must be a real calendar date in YYYY-MM-DD format.");

        for (String date : List.of("2028-02-30", "29-02-2028", "tomorrow")) {
            assertMessage("a/1 date/" + date + " d/2", expected);
        }
    }

    @Test
    public void parse_invalidDogIds_returnsSpecificMessageWithGuidance() {
        String expected = expected("Please provide positive, valid dog IDs separated by spaces.");

        for (String dogIds : List.of("0", "-2", "x", "2.0", "2,3", "2, 3", "999999999999")) {
            assertMessage("a/1 date/2028-02-29 d/" + dogIds, expected);
        }
    }

    @Test
    public void parse_duplicateDogIds_returnsSpecificMessageWithGuidance() {
        String expected = expected("Dog ID 2 is listed more than once. Please provide each dog ID only once.");

        assertMessage("a/1 date/2028-02-29 d/2 2", expected);
        assertMessage("a/1 date/2028-02-29 d/2 02", expected);
    }

    @Test
    public void parse_repeatedPrefix_returnsSpecificMessageWithGuidance() {
        assertMessage("a/1 a/2 date/2028-02-29 d/3",
                expected("Multiple values specified for the following single-valued field(s): a/"));
        assertMessage("a/1 date/2028-02-29 date/2028-03-01 d/3",
                expected("Multiple values specified for the following single-valued field(s): date/"));
        assertMessage("a/1 date/2028-02-29 d/2 d/3",
                expected("Multiple values specified for the following single-valued field(s): d/"));
    }

    @Test
    public void parse_invalidOverallStructure_returnsInvalidFormatWithGuidance() {
        String expected = expected("Invalid command format!");

        assertMessage("hello a/1 date/2028-02-29 d/2", expected);
        assertMessage("x/hello a/1 date/2028-02-29 d/2", expected);
    }

    @Test
    public void command_nonPositiveId_rejectsInvalidId() {
        assertThrows(IllegalArgumentException.class, () ->
                new EnrolDogCommand(0, LocalDate.of(2028, 2, 29), List.of(2)));
        assertThrows(IllegalArgumentException.class, () ->
                new EnrolDogCommand(1, LocalDate.of(2028, 2, 29), List.of(0)));
    }

    private String expected(String specificMessage) {
        return specificMessage + EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE;
    }

    private void assertMessage(String input, String expectedMessage) {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(input));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
