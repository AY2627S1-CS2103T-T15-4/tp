package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCustomerCommand;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class AddCustomerCommandParserTest {

    private final AddCustomerCommandParser parser = new AddCustomerCommandParser();
    private final String validArguments = " n/Alice Tan p/91234567 e/alice@example.com bc/98765432";

    @Test
    public void parse_allFieldsPresent_success() throws Exception {
        Customer customer = new Customer(new Name("Alice Tan"), new Phone("91234567"),
                new Email("alice@example.com"), new BackupContact("98765432"));

        assertParseSuccess(parser, validArguments, new AddCustomerCommand(customer));
        assertEquals(new AddCustomerCommand(customer),
                new AddressBookParser().parseCommand(AddCustomerCommand.COMMAND_WORD + validArguments));
    }

    @Test
    public void parse_missingRequiredField_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCustomerCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " p/91234567 e/alice@example.com bc/98765432", expectedMessage);
        assertParseFailure(parser, " n/Alice Tan e/alice@example.com bc/98765432", expectedMessage);
        assertParseFailure(parser, " n/Alice Tan p/91234567 bc/98765432", expectedMessage);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/alice@example.com", expectedMessage);
        assertParseFailure(parser, "unexpected" + validArguments, expectedMessage);
    }

    @Test
    public void parse_duplicateOrInvalidValue_failure() {
        assertParseFailure(parser, validArguments + " n/Bob", "Multiple values specified for the following "
                + "single-valued field(s): n/");
        assertParseFailure(parser, " n/Alice Tan p/12 e/alice@example.com bc/98765432",
                Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/invalid bc/98765432",
                Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/alice@example.com bc/12",
                BackupContact.MESSAGE_CONSTRAINTS);
    }
}
