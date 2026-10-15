package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCustomerCommand.EditCustomerDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

public class EditCustomerCommandTest {

    @Test
    public void execute_editsRequestedFieldAndPreservesCustomerId() throws Exception {
        Model model = new ModelManager();
        Customer original = customer(1, "Alice Tan", "91234567");
        model.addCustomer(original);
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setPhone(new Phone("98765432"));

        CommandResult result = new EditCustomerCommand(1, descriptor).execute(model);

        Customer expected = customer(1, "Alice Tan", "98765432");
        assertEquals(expected, model.getFilteredCustomerList().get(0));
        assertEquals(String.format(EditCustomerCommand.MESSAGE_EDIT_CUSTOMER_SUCCESS, expected),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_unknownCustomerId_throwsCommandException() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Tan"));

        assertThrows(CommandException.class, EditCustomerCommand.MESSAGE_CUSTOMER_NOT_FOUND, () ->
                new EditCustomerCommand(1, descriptor).execute(new ModelManager()));
    }

    @Test
    public void constructor_invalidCustomerId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EditCustomerCommand(0, new EditCustomerDescriptor()));
    }

    private static Customer customer(int id, String name, String phone) {
        return new Customer(id, new Name(name), new Phone(phone), new Email("test@example.com"),
                new BackupContact("98765432"));
    }
}
