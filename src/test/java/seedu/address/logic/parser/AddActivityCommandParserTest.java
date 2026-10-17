package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private final AddActivityCommandParser parser = new AddActivityCommandParser();

    @Test
    public void parse_validModesAndReorderedFields_success() throws Exception {
        for (Recurrence recurrence : Recurrence.values()) {
            AddActivityCommand expected = new AddActivityCommand(new ActivityName("Training"),
                   LocalDate.of(2028, 2, 29), LocalTime.of(9, 0), LocalTime.of(10, 0), recurrence);
            assertEquals(expected, parser.parse(VALID.replace("weekly", recurrence.name().toLowerCase(Locale.ROOT))));
        }
        assertEquals(parser.parse(VALID), parser.parse(
                " r/weekly end/10:00 n/  Training   start/09:00 date/2028-02-29"));
        ModelManager model = new ModelManager();
        new AddressBookParser().parseCommand("addactivity" + VALID).execute(model);
        Activity stored = model.getActivityList().get(0);
        assertEquals(1, stored.getId());
        assertEquals(LocalDate.of(2028, 2, 29), stored.getStartDate());
    }

    @Test
    public void parse_missingBlankDuplicateAndUnknownFields_failure() {
        assertThrows(ParseException.class, () -> parser.parse(""));
        assertThrows(ParseException.class, () -> parser.parse(" preamble" + VALID));
        for (String field : new String[]{"n/Training", "date/2028-02-29", "start/09:00", "end/10:00", "r/weekly"}) {
            assertThrows(ParseException.class, () -> parser.parse(VALID.replace(field, "")));
            assertThrows(ParseException.class, () -> parser.parse(VALID.replace(field,
                   field.substring(0, field.indexOf('/') + 1))));
            assertThrows(ParseException.class, () -> parser.parse(VALID + " " + field));
        }
        assertThrows(ParseException.class, () -> parser.parse(VALID + " d/1"));
        assertThrows(ParseException.class, () -> parser.parse(VALID.replace("Training", "Training x/unused")));
        assertThrows(ParseException.class, () -> parser.parse(VALID.replace("n/", "N/")));
    }

    @Test
    public void parse_invalidDatesTimesAndRecurrence_failure() {
        for (String date : new String[]{"2027-02-29", "2028-04-31", "2028-13-01", "2028-2-01", "tomorrow"}) {
            assertThrows(ParseException.class, AddActivityCommandParser.MESSAGE_DATE, () ->
                parser.parse(VALID.replace("2028-02-29", date)));
        }
        for (String time : new String[]{"24:00", "09:60", "9:00", "09:00:00", "noon", "-1:00"}) {
            assertThrows(ParseException.class, AddActivityCommandParser.MESSAGE_TIME, () ->
                parser.parse(VALID.replace("09:00", time)));
            assertThrows(ParseException.class, AddActivityCommandParser.MESSAGE_TIME, () ->
                parser.parse(VALID.replace("10:00", time)));
        }
        for (String recurrence : new String[]{"Weekly", "WEEKLY", "monthly"}) {
            assertThrows(ParseException.class, AddActivityCommandParser.MESSAGE_RECURRENCE, () ->
                parser.parse(VALID.replace("weekly", recurrence)));
        }
        for (String end : new String[]{"09:00", "08:59", "00:00"}) {
            assertThrows(ParseException.class, Activity.MESSAGE_TIME_CONSTRAINTS, () ->
                parser.parse(VALID.replace("10:00", end)));
        }
    }

    @Test
    public void parse_pastDateAndTimeBoundaries_success() throws Exception {
        assertEquals(new AddActivityCommand(new ActivityName("Walk & play"), LocalDate.of(2000, 1, 1),
                LocalTime.MIDNIGHT, LocalTime.of(23, 59), Recurrence.ONCE), parser.parse(
                       " n/Walk & play date/2000-01-01 start/00:00 end/23:59 r/once"));
    }
}
