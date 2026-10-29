// @@author Kenneth-Chia
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddActivityCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

public class AddActivityCommandParserTest {
    private static final String VALID = " n/Training date/2028-02-29 start/09:00 end/10:00 r/weekly";
    private static final List<String> VALID_FIELDS = List.of(
            "n/Training", "date/2028-02-29", "start/09:00", "end/10:00", "r/weekly");
    private final AddActivityCommandParser parser = new AddActivityCommandParser();

    @Test
    public void parse_validModesAndReorderedFields_success() throws Exception {
        for (Recurrence recurrence : Recurrence.values()) {
            AddActivityCommand expected = new AddActivityCommand(new ActivityName("Training"),
                   LocalDate.of(2028, 2, 29), LocalTime.of(9, 0), LocalTime.of(10, 0), recurrence);
            assertEquals(expected, parser.parse(VALID.replace("weekly", recurrence.name().toLowerCase(Locale.ROOT))),
                    recurrence.name());
        }
        assertEquals(parser.parse(VALID), parser.parse(
                " r/weekly end/10:00 n/  Training   start/09:00 date/2028-02-29"));
        ModelManager model = new ModelManager();
        // @@author
        new WatchDogParser().parseCommand("addactivity" + VALID).execute(model);
        // @@author Kenneth-Chia
        Activity stored = model.getActivityList().get(0);
        assertEquals(1, stored.getId());
        assertEquals(LocalDate.of(2028, 2, 29), stored.getStartDate());
    }

    @Test
    public void parse_emptyInputOrPreamble_failure() {
        assertThrows(ParseException.class, () -> parser.parse(""));
        assertThrows(ParseException.class, () -> parser.parse(" preamble" + VALID));
    }

    @Test
    public void parse_missingField_failure() {
        for (String field : VALID_FIELDS) {
            assertThrows(ParseException.class, () -> parser.parse(VALID.replace(field, "")), "Missing " + field);
        }
    }

    @Test
    public void parse_blankField_failure() {
        for (String field : VALID_FIELDS) {
            String input = VALID.replace(field, field.substring(0, field.indexOf('/') + 1));
            assertThrows(ParseException.class, () -> parser.parse(input), "Blank " + field);
        }
    }

    @Test
    public void parse_duplicateField_failure() {
        for (String field : VALID_FIELDS) {
            assertThrows(ParseException.class, () -> parser.parse(VALID + " " + field), "Duplicate " + field);
        }
    }

    @Test
    public void parse_unknownPrefix_failure() {
        for (String input : List.of(VALID + " d/1", VALID.replace("Training", "Training x/unused"),
                VALID.replace("n/", "N/"))) {
            assertThrows(ParseException.class, () -> parser.parse(input), input);
        }
    }

    @Test
    public void parse_invalidDate_failure() {
        for (String date : new String[]{"2027-02-29", "2028-04-31", "2028-13-01", "2028-2-01", "tomorrow"}) {
            assertParseFailure(VALID.replace("2028-02-29", date), AddActivityCommandParser.MESSAGE_DATE);
        }
    }

    @Test
    public void parse_invalidTime_failure() {
        for (String time : new String[]{"24:00", "09:60", "9:00", "09:00:00", "noon", "-1:00"}) {
            assertParseFailure(VALID.replace("09:00", time), AddActivityCommandParser.MESSAGE_TIME);
            assertParseFailure(VALID.replace("10:00", time), AddActivityCommandParser.MESSAGE_TIME);
        }
    }

    @Test
    public void parse_invalidRecurrence_failure() {
        for (String recurrence : new String[]{"Weekly", "WEEKLY", "monthly"}) {
            assertParseFailure(VALID.replace("weekly", recurrence), AddActivityCommandParser.MESSAGE_RECURRENCE);
        }
    }

    @Test
    public void parse_endNotAfterStart_failure() {
        for (String end : new String[]{"09:00", "08:59", "00:00"}) {
            assertParseFailure(VALID.replace("10:00", end), Activity.MESSAGE_TIME_CONSTRAINTS);
        }
    }

    @Test
    public void parse_pastDateAndTimeBoundaries_success() throws Exception {
        assertEquals(new AddActivityCommand(new ActivityName("Walk & play"), LocalDate.of(2000, 1, 1),
                LocalTime.MIDNIGHT, LocalTime.of(23, 59), Recurrence.ONCE), parser.parse(
                       " n/Walk & play date/2000-01-01 start/00:00 end/23:59 r/once"));
    }

    @Test
    public void parse_oneMinuteSchedulesAtDayBoundaries_success() throws Exception {
        assertEquals(new AddActivityCommand(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.MIDNIGHT, LocalTime.of(0, 1), Recurrence.WEEKLY), parser.parse(
                        VALID.replace("start/09:00 end/10:00", "start/00:00 end/00:01")));
        assertEquals(new AddActivityCommand(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(23, 58), LocalTime.of(23, 59), Recurrence.WEEKLY), parser.parse(
                        VALID.replace("start/09:00 end/10:00", "start/23:58 end/23:59")));
    }

    private void assertParseFailure(String input, String expectedMessage) {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse(input), input);
        assertEquals(expectedMessage, exception.getMessage(), input);
    }
}
// @@author
