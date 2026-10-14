package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.storage.JsonAddressBookStorage;

/** Exercises customer search parsing and execution against the real model. */
public class SearchCustomerCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager();

    @Test
    public void execute_nameSearch_filtersAllMatchesWithoutChangingRecords() throws Exception {
        Customer first = customer(0, "Alex Tan");
        Customer second = customer(7, "Alex Tan");
        Customer third = customer(42, "Alexandra Lim");
        model.addCustomer(first);
        model.addCustomer(second);
        model.addCustomer(third);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals("3 customers found.", execute("search customer n/aLeX"));
        assertEquals(List.of(first, second, third), model.getFilteredCustomerList());
        assertEquals("2 customers found.", execute("search customer n/alex tan"));
        assertEquals(List.of(first, second), model.getFilteredCustomerList());
        assertEquals("0 customers found.", execute("search customer n/alex  tan"));
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_idSearch_usesPersistentIdAndReplacesPreviousFilter() throws Exception {
        Customer first = customer(0, "Alex Tan");
        Customer second = customer(42, "Beth Lim");
        model.addCustomer(first);
        model.addCustomer(second);
        assertEquals("1 customers found.", execute("search customer n/alex"));
        assertEquals("1 customers found.", execute("search customer id/42"));
        assertEquals(List.of(second), model.getFilteredCustomerList());
        assertEquals("0 customers found.", execute("search customer id/1"));
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals("1 customers found.", execute("search customer id/0"));
        assertEquals(List.of(first), model.getFilteredCustomerList());
        new ListCommand().execute(model);
        assertEquals(List.of(first, second), model.getFilteredCustomerList());
    }

    @Test
    public void execute_emptyBook_returnsNoMatches() throws Exception {
        assertEquals("0 customers found.", execute("search customer n/Alex"));
        assertEquals("0 customers found.", execute("search customer id/0"));
    }

    @Test
    public void parse_invalidInput_preservesResultsAndRecords() throws Exception {
        Customer alex = customer(42, "Alex Tan");
        model.addCustomer(alex);
        execute("search customer n/alex");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(ParseException.class, () -> parser.parseCommand("search customer id/-1"));
        assertEquals(List.of(alex), model.getFilteredCustomerList());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_loadedCustomer_searchesPersistedId(@TempDir Path directory) throws Exception {
        Customer customer = customer(42, "Alex Tan");
        model.addCustomer(customer);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        Model loadedModel = new ModelManager(storage.readAddressBook().orElseThrow(),
                new UserPrefs());
        parser.parseCommand("search customer id/42").execute(loadedModel);
        assertEquals(List.of(customer), loadedModel.getFilteredCustomerList());
    }

    @Test
    public void constructorAndExecute_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchCustomerCommand(null));
        SearchCustomerCommand command = new SearchCustomerCommand(new CustomerSearchPredicate("Alex"));
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals_sameSearchPredicateOnly() {
        SearchCustomerCommand command = new SearchCustomerCommand(new CustomerSearchPredicate("Alex"));
        assertEquals(command, command);
        assertEquals(command, new SearchCustomerCommand(new CustomerSearchPredicate("Alex")));
        assertNotEquals(command, new SearchCustomerCommand(new CustomerSearchPredicate("Beth")));
        assertNotEquals(command, new SearchCustomerCommand(new CustomerSearchPredicate(42)));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
    }

    private String execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }

    private Customer customer(int id, String name) {
        return new Customer(id, name, "91234567", "alex@example.com", "98765432");
    }
}
