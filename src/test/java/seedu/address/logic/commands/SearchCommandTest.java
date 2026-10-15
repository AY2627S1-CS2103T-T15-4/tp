package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests global searches against both real model lists. */
public class SearchCommandTest {
    private final Customer alex = new Customer(42, "Alex Tan", "91234567", "alex@example.com", "98765432");
    private final Dog dog = new Dog(42, new DogName("Alex Buddy"), new Breed("Poodle"), new Size("small"));
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_searchesBothCategoriesAndReplacesFilters() throws Exception {
        AddressBook book = new AddressBook();
        book.addCustomer(alex);
        book.addDogFromStorage(dog);
        ModelManager model = new ModelManager(book, new UserPrefs());
        model.updateFilteredCustomerList(customer -> false);
        model.updateFilteredDogList(item -> false);

        assertEquals("1 customers and 1 dogs found.", execute(model, "search n/aLeX"));
        assertEquals(List.of(alex), model.getFilteredCustomerList());
        assertEquals(List.of(dog), model.getFilteredDogList());
        assertEquals("1 customers and 0 dogs found.", execute(model, "search n/alex tan"));
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals("0 customers and 1 dogs found.", execute(model, "search n/bud"));
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals("0 customers and 0 dogs found.", execute(model, "search n/missing"));
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());

        assertEquals("1 customers and 1 dogs found.", execute(model, "search id/42"));
        assertEquals(List.of(alex), model.getFilteredCustomerList());
        assertEquals(List.of(dog), model.getFilteredDogList());
        assertThrows(ParseException.class, () -> parser.parseCommand("search id/-1"));
        assertEquals(List.of(alex), model.getFilteredCustomerList());
        assertEquals(List.of(dog), model.getFilteredDogList());
        assertEquals("0 customers and 0 dogs found.", execute(model, "search id/0"));
        assertEquals(book, model.getAddressBook());
    }

    @Test
    public void execute_emptyBook_returnsZeroCounts() throws Exception {
        ModelManager model = new ModelManager();
        assertEquals("0 customers and 0 dogs found.", execute(model, "search n/alex"));
        assertEquals("0 customers and 0 dogs found.", execute(model, "search id/42"));
    }

    @Test
    public void invalidArguments_throwException() {
        assertThrows(NullPointerException.class, () -> new SearchCommand(null));
        assertThrows(IllegalArgumentException.class, () -> new SearchCommand(" "));
        assertThrows(IllegalArgumentException.class, () -> new SearchCommand(-1));
        assertThrows(NullPointerException.class, () -> new SearchCommand(42).execute(null));
    }

    @Test
    public void equals_sameFieldAndValueOnly() {
        SearchCommand nameSearch = new SearchCommand("Alex");
        assertEquals(nameSearch, nameSearch);
        assertEquals(nameSearch, new SearchCommand("Alex"));
        assertNotEquals(nameSearch, new SearchCommand("Beth"));
        assertNotEquals(nameSearch, new SearchCommand(42));
        assertNotEquals(nameSearch, null);
        assertNotEquals(nameSearch, new Object());
        assertEquals(new SearchCommand(42), new SearchCommand(42));
        assertNotEquals(new SearchCommand(42), new SearchCommand(43));
    }

    private String execute(ModelManager model, String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }
}
