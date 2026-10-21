package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class FilterCustomerCommandParserTest {
    private final FilterCustomerCommandParser parser = new FilterCustomerCommandParser();

    @Test
    public void parse_validFields_normalizesValues() throws Exception {
        FilterCustomerCommand expected = new FilterCustomerCommand(
                "Anna Lee", "91234567", "anna@example.com", "98765432", Set.of(12, 34));

        assertEquals(expected, parser.parse(" n/Anna   Lee p/91234567 e/ANNA@example.com d/12 34 bc/98765432 "));
    }

    @Test
    public void parse_invalidFormatOrDuplicateFields_throwsParseException() {
        String[] invalidInputs = {"", " ", "customer", "x/value", "N/Anna", "n/Anna n/Beth",
            "n/Anna p/1234567", "n/Anna p/91234567 p/92345678", "n/Anna e/a@b.com e/c@d.com",
            "n/Anna bc/91234567 bc/92345678", "n/Anna d/12 d/13", "extra n/Anna", "n/"};

        for (String invalidInput : invalidInputs) {
            assertThrows(ParseException.class, () -> parser.parse(invalidInput));
        }
    }

    @Test
    public void parse_invalidPhoneBackupOrDogId_throwsParseException() {
        for (String input : new String[]{"p/9123456", "bc/912345678", "d/-1", "d/abc"}) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }
}
