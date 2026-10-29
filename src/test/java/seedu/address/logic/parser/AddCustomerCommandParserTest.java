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
                new WatchDogParser().parseCommand(AddCustomerCommand.COMMAND_WORD + validArguments));
    }

    @Test
    public void parse_apostrophesAndSpacedPhoneNumbers_success() throws Exception {
        Customer customer = new Customer(new Name("O'Neil"), new Phone("91234567"),
                new Email("o'neil@example.com"), new BackupContact("98765432"));

        assertParseSuccess(parser, " n/O'Neil p/9 1 2 3 4 5 6 7 e/o'neil@example.com bc/9 8 7 6 5 4 3 2",
                new AddCustomerCommand(customer));
    }

    @Test
    public void parse_missingRequiredField_failure() {
        String usage = AddCustomerCommand.MESSAGE_USAGE;

        assertParseFailure(parser, " p/91234567 e/alice@example.com bc/98765432",
                "Missing required field(s): name.\n" + usage);
        assertParseFailure(parser, " n/Alice Tan e/alice@example.com bc/98765432",
                "Missing required field(s): phone.\n" + usage);
        assertParseFailure(parser, " n/Alice Tan p/91234567 bc/98765432",
                "Missing required field(s): email.\n" + usage);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/alice@example.com",
                "Missing required field(s): backup contact.\n" + usage);
        assertParseFailure(parser, " n/Alice Tan",
                "Missing required field(s): phone, email, backup contact.\n" + usage);
        assertParseFailure(parser, "unexpected" + validArguments,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }

    @Test
    public void parse_duplicateOrInvalidValue_failure() {
        assertParseFailure(parser, validArguments + " n/Bob", "Multiple values specified for the following "
                + "single-valued field(s): n/");
        assertParseFailure(parser, " n/Alice Tan p/12 e/alice@example.com bc/98765432",
                "Invalid phone: " + Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Bad@Name p/12 e/invalid bc/12",
                "Invalid name: " + Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice Tan p/12 e/invalid bc/12",
                "Invalid phone: " + Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/invalid bc/98765432",
                "Invalid email: " + Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice Tan p/91234567 e/alice@example.com bc/12",
                "Invalid backup contact: " + BackupContact.MESSAGE_CONSTRAINTS);
    }
}
