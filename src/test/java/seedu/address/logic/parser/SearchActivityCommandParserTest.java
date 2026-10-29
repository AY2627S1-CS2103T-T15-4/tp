package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.Recurrence;

public class SearchActivityCommandParserTest {
    private final SearchActivityCommandParser parser = new SearchActivityCommandParser();

    @Test
    public void parse_activityId_acceptsSingleAndCombinedFields() throws Exception {
        assertEquals(new SearchActivityCommand(null, null, null, null, 12), parser.parse(" i/ 12 "));
        assertEquals(new SearchActivityCommand("dog run", LocalDate.of(2026, 10, 20),
                LocalTime.NOON, Recurrence.WEEKLY, 12),
                parser.parse("i/12 n/Dog   run d/2026-10-20 t/12:00 r/Weekly"));
        for (String input : new String[]{"i/1 i/2", "i/", "I/1", "extra i/1"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_invalidActivityId_usesSpecificError() {
        for (String value : new String[]{"0", "-1", "abc", "2147483648", "-2147483649", "1 2"}) {
            assertThrows(ParseException.class,
                    "Error in searching activities:\nPlease enter a valid activity index", () ->
                    parser.parse("i/" + value));
        }
    }

    @Test
    public void parse_validFields_success() throws Exception {
        SearchActivityCommand expected = new SearchActivityCommand(
                "dog run", LocalDate.of(2026, 10, 20), LocalTime.of(16, 0), Recurrence.WEEKLY);

        assertEquals(expected, parser.parse(" n/Dog    run  d/2026-10-20 t/16:00 r/wEeKlY "));
        assertEquals(new SearchActivityCommand(null, null, LocalTime.MIDNIGHT),
                parser.parse("t/00:00"));
    }

    @Test
    public void parse_missingDuplicateUnknownOrUppercaseFields_throwsParseException() {
        String[] invalidInputs = {"", " ", "extra", "run n/walk", "n/walk n/run", "x/run", "N/run",
            "n/run x/value", "n/run d/2026-10-20 extra", "n/", "n/run d/2026-10-20 d/2026-10-21",
            "t/10:00 t/11:00"};

        for (String invalidInput : invalidInputs) {
            assertThrows(ParseException.class, () -> parser.parse(invalidInput));
        }
    }

    @Test
    public void parse_invalidDate_throwsDateParseException() {
        for (String date : new String[]{"2026-02-29", "2026-13-01", "2026-1-01", "tomorrow"}) {
            assertThrows(ParseException.class, () -> parser.parse("d/" + date));
        }
    }

    @Test
    public void parse_invalidDate_includesSearchErrorPrefix() {
        String expectedMessage = "Error in searching activities:\n"
                + "Activity date must be a valid date in YYYY-MM-DD format.";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse("d/2026-02-29"));
    }

    @Test
    public void parse_invalidTime_throwsTimeParseException() {
        for (String time : new String[]{"24:00", "09:60", "9:00", "09:00:00", "noon"}) {
            assertThrows(ParseException.class, () -> parser.parse("t/" + time));
        }
    }

    @Test
    public void parse_invalidTime_includesSearchErrorPrefix() {
        String expectedMessage = "Error in searching activities:\n"
                + "Activity times must use 24-hour HH:mm format (00:00 to 23:59).";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse("t/24:00"));
    }

    @Test
    public void parse_invalidOrDuplicateRecurrence_throwsParseException() {
        for (String input : new String[]{"r/monthly", "r/weekly r/daily", "R/once"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_invalidRecurrence_includesSearchErrorPrefix() {
        String expectedMessage = "Error in searching activities:\n"
                + "Activity recurrence must be Once, Weekly, or Daily.";
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse("r/monthly"));
    }

    @Test
    public void parse_eachSupportedRecurrence_isAcceptedCaseInsensitively() throws Exception {
        assertEquals(new SearchActivityCommand(null, null, null, Recurrence.ONCE),
                parser.parse("r/once"));
        assertEquals(new SearchActivityCommand(null, null, null, Recurrence.WEEKLY),
                parser.parse("r/WEEKLY"));
        assertEquals(new SearchActivityCommand(null, null, null, Recurrence.DAILY),
                parser.parse("r/ Daily "));
    }

    @Test
    public void parse_fieldsWithLeadingAndTrailingWhitespace_success() throws Exception {
        assertEquals(new SearchActivityCommand("walk", null, null), parser.parse("  n/  Walk  "));
        assertEquals(new SearchActivityCommand(null, LocalDate.of(2026, 10, 20), null),
                parser.parse(" d/2026-10-20 "));
    }
}
