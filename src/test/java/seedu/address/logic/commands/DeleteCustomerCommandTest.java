package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;

/** Contains tests for {@link DeleteCustomerCommand}. */
public class DeleteCustomerCommandTest {

    private static final Customer LILY = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
    private static final Customer JOHN = new Customer(2, "John Doe", "81234567", "john@gmail.com", "81234568");

    @Test
    public void constructor_nonPositiveCustomerId_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteCustomerCommand(0));
        assertThrows(IllegalArgumentException.class, () -> new DeleteCustomerCommand(-1));
    }

    @Test
    public void execute_existingCustomer_deletesCustomer() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addCustomer(LILY);

        CommandResult result = new DeleteCustomerCommand(LILY.getId()).execute(model);

        assertEquals("Successfully deleted customer: Lily Koh; id: 1; Contact: 91234567; "
                + "Email: lily@gmail.com; Backup contact: 98765432", result.getFeedbackToUser());
        assertEquals(0, model.getCustomerList().size());
    }

    @Test
    public void execute_secondCustomer_deletesMatchingCustomer() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addCustomer(LILY);
        model.addCustomer(JOHN);

        new DeleteCustomerCommand(JOHN.getId()).execute(model);

        assertEquals(1, model.getCustomerList().size());
        assertEquals("Lily Koh", model.getCustomerList().get(0).getName().fullName);
    }

    @Test
    public void execute_missingCustomer_throwsCommandException() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());

        assertThrows(CommandException.class, DeleteCustomerCommand.MESSAGE_INVALID_ID, () ->
                new DeleteCustomerCommand(99).execute(model));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteCustomerCommand(21).execute(null));
    }

    @Test
    public void equals_sameId_returnsTrue() {
        assertTrue(new DeleteCustomerCommand(21).equals(new DeleteCustomerCommand(21)));
    }

    @Test
    public void equals_sameObject_returnsTrue() {
        DeleteCustomerCommand command = new DeleteCustomerCommand(21);
        assertTrue(command.equals(command));
    }

    @Test
    public void equals_differentId_returnsFalse() {
        assertFalse(new DeleteCustomerCommand(21).equals(new DeleteCustomerCommand(22)));
    }

    @Test
    public void equals_null_returnsFalse() {
        assertFalse(new DeleteCustomerCommand(21).equals(null));
    }

    @Test
    public void equals_differentType_returnsFalse() {
        assertFalse(new DeleteCustomerCommand(21).equals(new Object()));
    }

    @Test
    public void toString_containsCustomerId() {
        assertEquals("seedu.address.logic.commands.DeleteCustomerCommand{customerId=21}",
                new DeleteCustomerCommand(21).toString());
    }
}
