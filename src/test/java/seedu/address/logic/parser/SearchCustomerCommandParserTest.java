package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

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
        for (String input : new String[]{"p/9123456", "bc/912345678", "d/-1", "d/abc",
            "i/0", "i/00", "i/-1", "i/abc"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void parse_invalidDogOrCustomerId_includesSearchingErrorPrefix() {
        for (String value : new String[]{"-1", "0", "00", "not-a-number", "2147483648", "-2147483649"}) {
            assertThrows(ParseException.class, "Error in searching customers:\nPlease enter a valid dog index", () ->
                    parser.parse("d/" + value));
            assertThrows(ParseException.class, "Error in searching customers:\nPlease enter a valid dog index", () ->
                    parser.parse("d/1 " + value));
            assertThrows(ParseException.class,
                    "Error in searching customers:\nPlease enter a valid customer index", () ->
                    parser.parse("i/" + value));
        }
    }
}
