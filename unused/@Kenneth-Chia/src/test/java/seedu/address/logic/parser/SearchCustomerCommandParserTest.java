package seedu.address.logic.parser;
//@@author Kenneth-Chia-unused
// Original category-search parser tests, superseded by searchc tests in PR #150;
// retained for authorship and reference.

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.customer.CustomerSearchPredicate;

public class SearchCustomerCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_validInput_createsExpectedCommand() throws Exception {
        assertEquals(new SearchCustomerCommand(new CustomerSearchPredicate("Little Bud")),
                parser.parseCommand("  search\tcustomer\t n/ Little Bud  "));
        assertEquals(new SearchCustomerCommand(new CustomerSearchPredicate(0)),
                parser.parseCommand("search customer id/0"));
        assertEquals(new SearchCustomerCommand(new CustomerSearchPredicate(12)),
                parser.parseCommand("search customer id/0012"));
        assertEquals(new SearchCustomerCommand(new CustomerSearchPredicate(Integer.MAX_VALUE)),
                parser.parseCommand("search customer id/2147483647"));
    }

    @Test
    public void parse_invalidInput_throwsParseException() {
        String[] invalidInputs = {"search customer", "search customer Buddy",
            "search customer n/", "search customer n/   ", "search customer id/", "search customer id/-1",
            "search customer id/+1",
            "search customer id/1.5", "search customer id/2147483648", "search customer id/one",
            "search customer id/1 2",
            "search customer n/Buddy id/0", "search customer id/0 n/Buddy", "search customer n/Buddy n/Max",
            "search customer id/0 id/1", "search customer b/Poodle", "search customer n/Buddy b/Poodle",
            "search customer extra n/Buddy"};
        for (String input : invalidInputs) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand(input), input);
            assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    SearchCustomerCommand.MESSAGE_USAGE), exception.getMessage());
        }
    }

    @Test
    public void parse_blankNameValue_rejectedByCustomerParser() {
        ParseException exception = assertThrows(ParseException.class, () ->
                new SearchCustomerCommandParser().parse("customer n/   "));
        assertThrows(ParseException.class, () ->
                new SearchCustomerCommandParser().parse("customer n/\u2003"));
        assertEquals(String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                SearchCustomerCommand.MESSAGE_USAGE), exception.getMessage());
    }
}
