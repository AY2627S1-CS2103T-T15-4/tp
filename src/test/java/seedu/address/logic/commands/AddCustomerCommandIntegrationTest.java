package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.storage.JsonAddressBookStorage;

public class AddCustomerCommandIntegrationTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_exhaustedIds_reportsErrorAfterDeletionAndReload(@TempDir Path directory) throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.setNextCustomerId(Integer.MAX_VALUE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        execute(model, "Alice Tan", "91234567", "alice@example.com");
        assertEquals(Integer.MAX_VALUE, model.getCustomerList().get(0).getId());

        assertThrows(CommandException.class,
                "No more customer IDs are available", () -> execute(model, "Bob Lim", "92345678", "bob@example.com"));
        assertEquals(1, model.getCustomerList().size());

        model.deleteCustomer(model.getCustomerList().get(0));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());

        assertThrows(CommandException.class,
                "No more customer IDs are available", () -> execute(
                        reloadedModel, "Carol Lee", "93456789", "carol@example.com"));
        assertEquals(0, reloadedModel.getCustomerList().size());
    }

    @Test
    public void execute_assignsNonRepeatingIdsAndPreservesThemAfterReload(@TempDir Path directory) throws Exception {
        Model model = new ModelManager();

        execute(model, "Alice Tan", "91234567", "alice@example.com");
        execute(model, "Bob Lim", "92345678", "bob@example.com");
        Customer alice = model.getFilteredCustomerList().get(0);
        model.deleteCustomer(alice);
        execute(model, "Carol Lee", "93456789", "carol@example.com");

        assertEquals(List.of(2, 3), model.getFilteredCustomerList().stream().map(Customer::getId).toList());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("data").resolve("watchdog.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());

        execute(reloadedModel, "David Tan", "94567890", "david@example.com");
        assertEquals(List.of(2, 3, 4), reloadedModel.getFilteredCustomerList().stream()
                .map(Customer::getId)
                .toList());
    }

    private void execute(Model model, String name, String phone, String email) throws Exception {
        parser.parseCommand("addcustomer n/" + name + " p/" + phone + " e/" + email + " bc/98765432")
                .execute(model);
    }
}
