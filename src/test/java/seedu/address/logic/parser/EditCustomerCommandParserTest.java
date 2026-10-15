package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCustomerCommand;
import seedu.address.logic.commands.EditCustomerCommand.EditCustomerDescriptor;
import seedu.address.model.customer.Name;

public class EditCustomerCommandParserTest {

    private final EditCustomerCommandParser parser = new EditCustomerCommandParser();

    @Test
    public void parse_validInput_success() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("New Name"));

        assertParseSuccess(parser, " 1 n/New Name", new EditCustomerCommand(1, descriptor));
    }

    @Test
    public void parse_missingIdOrEditedField_failure() {
        assertParseFailure(parser, " n/New Name", "Customer ID must be a positive integer.");
        assertParseFailure(parser, " 1", EditCustomerCommand.MESSAGE_NOT_EDITED);
    }
}
