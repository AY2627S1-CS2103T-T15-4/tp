package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCustomerCommand;
import seedu.address.logic.commands.EditCustomerCommand.EditCustomerDescriptor;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class EditCustomerCommandParserTest {

    private final EditCustomerCommandParser parser = new EditCustomerCommandParser();

    @Test
    public void parse_validInput_success() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("New Name"));

        assertParseSuccess(parser, " 1 n/New Name", new EditCustomerCommand(1, descriptor));
    }

    @Test
    public void parse_allEditableFields_success() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("New Name"));
        descriptor.setPhone(new Phone("98765432"));
        descriptor.setEmail(new seedu.address.model.customer.Email("new@example.com"));
        descriptor.setBackupContact(new seedu.address.model.customer.BackupContact("92345678"));

        assertParseSuccess(parser, " 1 n/New Name p/98765432 e/new@example.com bc/92345678",
                new EditCustomerCommand(1, descriptor));
    }

    @Test
    public void parse_missingIdOrEditedField_failure() {
        assertParseFailure(parser, " n/New Name", "Please provide a valid positive customer ID");
        assertParseFailure(parser, " 1", EditCustomerCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, " 0 n/New Name", "Please provide a valid positive customer ID");
        assertParseFailure(parser, " invalid n/New Name", "Please provide a valid positive customer ID");
    }

    @Test
    public void parse_outOfRangeId_failure() {
        assertParseFailure(parser, " 2147483648 n/Alice", "Please provide a valid positive customer ID");
        assertParseFailure(parser, " 999999999999999999999999999999999 n/Alice",
                "Please provide a valid positive customer ID");
    }

    @Test
    public void parse_duplicateOrInvalidField_failure() {
        assertParseFailure(parser, " 1 n/New Name n/Other", "Multiple values specified for the following "
                + "single-valued field(s): n/");
        assertParseFailure(parser, " 1 p/12", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 e/invalid", seedu.address.model.customer.Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 bc/12", seedu.address.model.customer.BackupContact.MESSAGE_CONSTRAINTS);
    }
}
