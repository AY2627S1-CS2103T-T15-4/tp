// @@author XanderGoh
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.testutil.Assert;

public class SearchCustomerCommandParserTest {
    private final SearchCustomerCommandParser parser = new SearchCustomerCommandParser();

    @Test
    public void parse_validFields_normalizesValues() throws Exception {
        SearchCustomerCommand expected = new SearchCustomerCommand(
                "Anna Lee", "91234567", "anna@example.com", "98765432", Set.of(12, 34), 7);

        assertEquals(expected, parser.parse(
                " n/Anna   Lee p/91234567 e/ANNA@example.com d/12 34 bc/98765432 i/7 "));
        assertEquals(new SearchCustomerCommand(null, null, null, null, null, 21), parser.parse("i/21"));
    }

    @Test
    public void parse_formattedLocalAndInternationalPhones_normalizesBothFields() throws Exception {
        assertEquals(new SearchCustomerCommand(null, "91234567", null, "+442012345678", null),
                parser.parse("p/+65 9123-4567 bc/+44 20-1234-5678"));
        assertEquals(new SearchCustomerCommand(null, "+1234567", null, "+123456789012345", null),
                parser.parse("p/+1234567 bc/+123456789012345"));
        assertEquals(new SearchCustomerCommand(null, "91234567", null, "98765432", null),
                parser.parse("p/9123-4567 bc/9876 5432"));
    }

    @Test
    public void parse_invalidInternationalPhones_rejectsBothFields() {
        for (String number : new String[]{"+123456", "+1234567890123456", "+01234567",
            "+44 (20) 1234 5678", "++1234567", "44 20 1234 5678", "91234567-",
            "+651234567", "+65123456789", "+6-5-123456789"}) {
            assertThrows(ParseException.class, () -> parser.parse("p/" + number));
            assertThrows(ParseException.class, () -> parser.parse("bc/" + number));
        }
    }

    @Test
    public void parse_invalidFormatOrDuplicateFields_throwsParseException() {
        String[] invalidInputs = {"", " ", "customer", "x/value", "N/Anna", "n/Anna n/Beth",
            "n/Anna x/value", "n/Anna p/1234567", "n/Anna p/91234567 p/92345678", "n/Anna e/a@b.com e/c@d.com",
            "n/Anna bc/91234567 bc/92345678", "n/Anna d/12 d/13", "i/12 i/13", "extra n/Anna", "n/"};

        for (String invalidInput : invalidInputs) {
            assertThrows(ParseException.class, () -> parser.parse(invalidInput));
        }
    }

    @Test
    public void parse_invalidPhoneBackupOrId_throwsParseException() {
        for (String input : new String[]{"p/9123456", "bc/912345678", "d/0", "d/-1", "d/abc", "i/-1", "i/abc"}) {

            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_invalidDogOrCustomerId_includesSearchingErrorPrefix() {
        for (String value : new String[]{"-1", "0", "00", "not-a-number", "2147483648", "-2147483649"}) {
            assertThrows(ParseException.class, "Error in searching customers:\nPlease enter a valid dog ID", () ->
                    parser.parse("d/" + value));
            assertThrows(ParseException.class, "Error in searching customers:\nPlease enter a valid dog ID", () ->
                    parser.parse("d/1 " + value));
            assertThrows(ParseException.class,
                    "Error in searching customers:\nPlease enter a valid customer ID", () ->
                    parser.parse("i/" + value));
        }
    }

    @Test
    public void command_zeroDogId_rejectsInvalidDogId() {
        Assert.assertThrows(IllegalArgumentException.class, () ->
                new SearchCustomerCommand(null, null, null, null, java.util.Set.of(0)));
    }
}
