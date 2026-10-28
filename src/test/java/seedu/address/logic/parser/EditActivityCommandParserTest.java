package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditActivityCommand;
import seedu.address.logic.commands.EditActivityCommand.EditActivityDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Tests parsing optional activity edits. */
public class EditActivityCommandParserTest {
    private final EditActivityCommandParser parser = new EditActivityCommandParser();

    @Test
    public void parse_allFieldsInAnyOrder_success() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));
        descriptor.setDate(LocalDate.of(2028, 2, 29));
        descriptor.setStartTime(LocalTime.of(9, 0));
        descriptor.setEndTime(LocalTime.of(10, 0));
        descriptor.setRecurrence(Recurrence.WEEKLY);
        EditActivityCommand expected = new EditActivityCommand(3, descriptor);

        assertEquals(expected, parser.parse(" 3 n/Training date/2028-02-29 start/09:00 end/10:00 r/weekly"));
        assertEquals(expected, parser.parse(" 3 r/weekly end/10:00 n/Training start/09:00 date/2028-02-29"));
    }

    @Test
    public void parse_oneField_success() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(10, 0));

        assertEquals(new EditActivityCommand(3, descriptor), parser.parse(" 3 end/10:00"));
    }

    @Test
    public void parse_eachOptionalField_success() throws Exception {
        EditActivityDescriptor name = new EditActivityDescriptor();
        name.setName(new ActivityName("Training"));
        EditActivityDescriptor date = new EditActivityDescriptor();
        date.setDate(LocalDate.of(2028, 3, 1));
        EditActivityDescriptor start = new EditActivityDescriptor();
        start.setStartTime(LocalTime.of(8, 30));
        EditActivityDescriptor once = new EditActivityDescriptor();
        once.setRecurrence(Recurrence.ONCE);
        EditActivityDescriptor daily = new EditActivityDescriptor();
        daily.setRecurrence(Recurrence.DAILY);

        assertEquals(new EditActivityCommand(1, name), parser.parse(" 1 n/Training"));
        assertEquals(new EditActivityCommand(3, date), parser.parse(" 3 date/2028-03-01"));
        assertEquals(new EditActivityCommand(3, start), parser.parse(" 3 start/08:30"));
        assertEquals(new EditActivityCommand(3, once), parser.parse(" 3 r/once"));
        assertEquals(new EditActivityCommand(3, daily), parser.parse(" 3 r/daily"));
    }

    @Test
    public void parse_missingInvalidAndRepeatedFields_failure() {
        for (String input : new String[]{"", " 0 n/Training", " 3", " -1 n/Training", " abc n/Training",
            " 2147483648 n/Training", " 3 n/", " 3 date/2027-02-29", " 3 date/2028/03/01",
            " 3 start/24:00", " 3 start/8:00", " 3 end/9:00", " 3 end/25:00",
            " 3 r/Weekly", " 3 n/Training n/Walk", " 3 d/7", " 3 n/Training x/unused"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
