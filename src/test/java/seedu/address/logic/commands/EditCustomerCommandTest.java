package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals("Edited customer: 1; Name: Alice Tan; Phone: 98765432; Email: test@example.com; "
                + "Backup Contact: 98765432", result.getFeedbackToUser());
    }

    @Test
    public void execute_unknownCustomerId_throwsCommandException() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Tan"));

        assertThrows(CommandException.class, EditCustomerCommand.MESSAGE_CUSTOMER_NOT_FOUND, () ->
                new EditCustomerCommand(1, descriptor).execute(new ModelManager()));
    }

    @Test
    public void execute_allFieldsEdited_preservesCustomerFilterAndId() throws Exception {
        Model model = new ModelManager();
        model.addCustomer(customer(1, "Alice Tan", "91234567"));
        model.updateFilteredCustomerList(customer -> false);
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Lee"));
        descriptor.setPhone(new Phone("98765432"));
        descriptor.setEmail(new Email("alice.lee@example.com"));
        descriptor.setBackupContact(new BackupContact("92345678"));

        new EditCustomerCommand(1, descriptor).execute(model);

        Customer expected = new Customer(1, new Name("Alice Lee"), new Phone("98765432"),
                new Email("alice.lee@example.com"), new BackupContact("92345678"));
        assertEquals(expected, model.getAddressBook().getCustomerList().get(0));
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_secondCustomerId_editsOnlyMatchingCustomer() throws Exception {
        Model model = new ModelManager();
        Customer first = customer(1, "Alice Tan", "91234567");
        Customer second = customer(2, "Bob Lim", "92345678");
        model.addCustomer(first);
        model.addCustomer(second);
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Bob Lee"));

        new EditCustomerCommand(2, descriptor).execute(model);

        assertEquals(first, model.getFilteredCustomerList().get(0));
        assertEquals(customer(2, "Bob Lee", "92345678"), model.getFilteredCustomerList().get(1));
    }

    @Test
    public void constructor_invalidCustomerId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EditCustomerCommand(0, new EditCustomerDescriptor()));
        assertThrows(NullPointerException.class, () -> new EditCustomerCommand(1, null));
    }

    @Test
    public void descriptor_accessorsEqualityAndToString() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        assertTrue(descriptor.getName().isEmpty());

        descriptor.setName(new Name("Alice Tan"));
        assertTrue(descriptor.isAnyFieldEdited());
        EditCustomerDescriptor copy = new EditCustomerDescriptor(descriptor);

        assertEquals(descriptor, copy);
        assertEquals(descriptor, descriptor);
        assertEquals(descriptor.hashCode(), copy.hashCode());
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals("descriptor"));
        assertTrue(descriptor.toString().contains("Alice Tan"));
    }

    @Test
    public void descriptor_equalsDifferentFieldValues_returnsFalse() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Tan"));
        descriptor.setPhone(new Phone("91234567"));
        descriptor.setEmail(new Email("alice@example.com"));
        descriptor.setBackupContact(new BackupContact("98765432"));

        assertFalse(descriptor.equals(descriptorWithName("Bob Lim")));
        assertFalse(descriptor.equals(descriptorWithPhone("92345678")));
        assertFalse(descriptor.equals(descriptorWithEmail("bob@example.com")));
        assertFalse(descriptor.equals(descriptorWithBackupContact("92345678")));
    }

    @Test
    public void equalsAndToString() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Tan"));
        EditCustomerCommand command = new EditCustomerCommand(1, descriptor);

        assertEquals(command, command);
        assertEquals(command, new EditCustomerCommand(1, descriptor));
        EditCustomerDescriptor differentDescriptor = new EditCustomerDescriptor();
        differentDescriptor.setName(new Name("Bob Lim"));
        assertFalse(command.equals(new EditCustomerCommand(1, differentDescriptor)));
        assertFalse(command.equals(new EditCustomerCommand(2, descriptor)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));
        assertTrue(command.toString().contains("customerId=1"));
    }

    private static Customer customer(int id, String name, String phone) {
        return new Customer(id, new Name(name), new Phone(phone), new Email("test@example.com"),
                new BackupContact("98765432"));
    }

    private static EditCustomerDescriptor descriptorWithName(String name) {
        EditCustomerDescriptor descriptor = fullDescriptor();
        descriptor.setName(new Name(name));
        return descriptor;
    }

    private static EditCustomerDescriptor descriptorWithPhone(String phone) {
        EditCustomerDescriptor descriptor = fullDescriptor();
        descriptor.setPhone(new Phone(phone));
        return descriptor;
    }

    private static EditCustomerDescriptor descriptorWithEmail(String email) {
        EditCustomerDescriptor descriptor = fullDescriptor();
        descriptor.setEmail(new Email(email));
        return descriptor;
    }

    private static EditCustomerDescriptor descriptorWithBackupContact(String backupContact) {
        EditCustomerDescriptor descriptor = fullDescriptor();
        descriptor.setBackupContact(new BackupContact(backupContact));
        return descriptor;
    }

    private static EditCustomerDescriptor fullDescriptor() {
        EditCustomerDescriptor descriptor = new EditCustomerDescriptor();
        descriptor.setName(new Name("Alice Tan"));
        descriptor.setPhone(new Phone("91234567"));
        descriptor.setEmail(new Email("alice@example.com"));
        descriptor.setBackupContact(new BackupContact("98765432"));
        return descriptor;
    }
}
