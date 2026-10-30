// @@author XanderGoh
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
    public void parse_invalidPhoneQueries_explainsReasonAndIdentifiesField() {
        String[][] cases = {
            {"", "Enter a complete phone number, e.g. 91234567 or +442012345678."},
            {"+55", "Enter the complete international number: + followed by 7 to 15 digits including the country code, "
                    + "e.g. +442012345678. Partial phone searches are not supported."},
            {"+1234567890123456", "Enter the complete international number: + followed by 7 to 15 digits "
                    + "including the country code, e.g. +442012345678. Partial phone searches are not supported."},
            {"+65 1234567", "Singapore numbers must contain exactly 8 digits after +65, e.g. +6591234567."},
            {"+6-5-123456789", "Singapore numbers must contain exactly 8 digits after +65, e.g. +6591234567."},
            {"+01234567", "The first digit after + must be 1-9."},
            {"9123", "Local numbers must contain exactly 8 digits without a country code, e.g. 91234567. "
                    + "For international numbers, include + and the country code. "
                    + "Partial phone searches are not supported."}
        };
        for (String[] example : cases) {
            assertPhoneQueryError(example[0], example[1]);
        }
    }

    @Test
    public void parse_invalidPhoneSymbols_distinguishesSymbolsFromFormatting() {
        for (String number : new String[]{"+44 (20) 1234 5678", "9123a567", "9123/4567", "9123\t4567"}) {
            assertPhoneQueryError(number, "Use digits and an optional leading +. "
                    + "Only spaces and hyphens are allowed between digits; "
                    + "letters, parentheses and other symbols are not allowed.");
        }
        for (String number : new String[]{"+", "++551234567", "55+1234567", "+ 551234567", "91234567-"}) {
            assertPhoneQueryError(number, "Place + only at the start, immediately before a digit. "
                    + "Spaces and hyphens must be between digits.");
        }
    }

    @Test
    public void parse_duplicatePhoneFields_requestsOnlyOneValue() {
        assertThrows(ParseException.class, "Error in searching customers:\nSpecify phone (p/) only once.", () ->
                parser.parse("p/91234567 p/+6591234567"));
        String backupMessage = "Error in searching customers:\nSpecify backup contact (bc/) only once.";
        assertThrows(ParseException.class, backupMessage, () ->
                parser.parse("bc/91234567 bc/+6591234567"));
    }

    private void assertPhoneQueryError(String number, String reason) {
        assertThrows(ParseException.class, "Error in searching customers:\nInvalid phone (p/): " + reason, () ->
                parser.parse("p/" + number));
        String backupMessage = "Error in searching customers:\nInvalid backup contact (bc/): " + reason;
        assertThrows(ParseException.class, backupMessage, () ->
                parser.parse("bc/" + number));
    }

    @Test
    public void parse_unsupportedPrefixes_reportsInvalidCommandFormat() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCustomerCommand.MESSAGE_USAGE);
        for (String query : new String[]{"x/value", "n/Anna x/value", "x/value p/91234567",
            "P/91234567", "BC/98765432", "phone/91234567"}) {
            assertThrows(ParseException.class, message, () -> parser.parse(query));
        }
    }

    @Test
    public void parse_multilinePhoneValue_rejectsUnconsumedInput() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchCustomerCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, message, () -> parser.parse("n/Anna p/9123\n4567"));
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
