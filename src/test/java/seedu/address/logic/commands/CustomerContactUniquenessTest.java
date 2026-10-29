package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;

public class CustomerContactUniquenessTest {
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addCustomer(new Customer(1, "Alice", "91234567", "alice@example.com", "98765432"));
        model.addCustomer(new Customer(2, "Bob", "92345678", "bob@example.com", "98765432"));
    }

    @Test
    public void add_duplicatePhoneIncludingSpaces_rejected() throws Exception {
        for (String phone : List.of("91234567", "9 1 2 3 4 5 6 7")) {
            assertRejected("addcustomer n/Carol p/" + phone + " e/carol@example.com bc/98765432",
                    CustomerContactValidator.MESSAGE_DUPLICATE_PHONE);
        }
    }

    @Test
    public void add_duplicateEmailIgnoringCase_rejected() throws Exception {
        for (String email : List.of("alice@example.com", "ALICE@EXAMPLE.COM")) {
            assertRejected("addcustomer n/Carol p/93456789 e/" + email + " bc/98765432",
                    CustomerContactValidator.MESSAGE_DUPLICATE_EMAIL);
        }
    }

    @Test
    public void add_hiddenCustomerContact_rejected() throws Exception {
        model.updateFilteredCustomerList(customer -> customer.getId() == 2);

        assertRejected("addcustomer n/Carol p/91234567 e/carol@example.com bc/98765432",
                CustomerContactValidator.MESSAGE_DUPLICATE_PHONE);
        assertRejected("addcustomer n/Carol p/93456789 e/alice@example.com bc/98765432",
                CustomerContactValidator.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void add_sharedBackupAndSameName_allowed() throws Exception {
        parser.parseCommand("addcustomer n/Alice p/93456789 e/carol@example.com bc/98765432").execute(model);
        parser.parseCommand("addcustomer n/David p/94567890 e/david@example.com bc/91234567").execute(model);

        assertEquals(new Customer(3, "Alice", "93456789", "carol@example.com", "98765432"),
                model.getCustomerList().get(2));
        assertEquals("91234567", model.getCustomerList().get(3).getBackupContact().value);
    }

    @Test
    public void add_primaryPhoneMatchingBackup_allowed() throws Exception {
        parser.parseCommand("addcustomer n/Carol p/98765432 e/carol@example.com bc/98765432").execute(model);

        assertEquals(3, model.getCustomerList().size());
    }

    @Test
    public void edit_duplicatePhoneOrEmail_rejected() throws Exception {
        assertRejected("editcustomer 2 p/9 1 2 3 4 5 6 7",
                CustomerContactValidator.MESSAGE_DUPLICATE_PHONE);
        assertRejected("editcustomer 2 e/alice@example.com", CustomerContactValidator.MESSAGE_DUPLICATE_EMAIL);
        assertRejected("editcustomer 2 e/ALICE@EXAMPLE.COM", CustomerContactValidator.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void edit_hiddenCustomerContact_rejected() throws Exception {
        model.updateFilteredCustomerList(customer -> customer.getId() == 2);

        assertRejected("editcustomer 2 n/Bob Lee p/91234567 e/new@example.com",
                CustomerContactValidator.MESSAGE_DUPLICATE_PHONE);
        assertRejected("editcustomer 2 n/Bob Lee p/93456789 e/alice@example.com",
                CustomerContactValidator.MESSAGE_DUPLICATE_EMAIL);
    }

    @Test
    public void edit_ownContactsAndSharedBackup_allowed() throws Exception {
        parser.parseCommand("editcustomer 1 n/Alice Lee p/9 1 2 3 4 5 6 7 e/ALICE@EXAMPLE.COM bc/92345678")
                .execute(model);
        parser.parseCommand("editcustomer 2 bc/92345678").execute(model);

        assertEquals(new Customer(1, "Alice Lee", "91234567", "ALICE@EXAMPLE.COM", "92345678"),
                model.getCustomerList().get(0));
        assertEquals("92345678", model.getCustomerList().get(1).getBackupContact().value);
    }

    @Test
    public void edit_uniqueContacts_allowedAndOldContactsReusable() throws Exception {
        parser.parseCommand("editcustomer 1 p/93456789 e/new@example.com").execute(model);
        parser.parseCommand("addcustomer n/Carol p/91234567 e/alice@example.com bc/98765432").execute(model);

        assertEquals(new Customer(1, "Alice", "93456789", "new@example.com", "98765432"),
                model.getCustomerList().get(0));
        assertEquals(3, model.getCustomerList().size());
    }

    @Test
    public void add_deletedCustomerContacts_allowed() throws Exception {
        parser.parseCommand("delete customer 1").execute(model);
        parser.parseCommand("addcustomer n/Carol p/91234567 e/alice@example.com bc/98765432").execute(model);

        assertEquals(3, model.getCustomerList().get(1).getId());
    }

    private void assertRejected(String input, String message) throws Exception {
        List<Customer> customersBefore = List.copyOf(model.getCustomerList());
        List<Customer> visibleBefore = List.copyOf(model.getFilteredCustomerList());
        long nextCustomerId = model.getAddressBook().getNextCustomerId();
        Command command = parser.parseCommand(input);

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals(message, exception.getMessage());
        assertEquals(customersBefore, model.getCustomerList());
        assertEquals(visibleBefore, model.getFilteredCustomerList());
        assertEquals(nextCustomerId, model.getAddressBook().getNextCustomerId());
    }
}
