package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private static final Customer LILY = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(LILY);
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_noCustomers_showsEmptyMessage() {
        Model emptyModel = new ModelManager();
        Model expectedModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel,
                new CommandResult(ListCommand.MESSAGE_EMPTY), expectedModel);
    }

    @Test
    public void execute_listCustomerThenSearchDog_showsDogsAgain() throws Exception {
        Dog cookie = model.addDog(new Dog(new DogName("Cookie"), new Breed("Chihuahua"), new Size("Small")));

        assertEquals(ListCommand.MESSAGE_SUCCESS, new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(LILY), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());

        assertEquals("1 dogs found.",
                new SearchDogCommand(new DogSearchPredicate("Cookie")).execute(model).getFeedbackToUser());
        assertEquals(List.of(cookie), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());

        assertEquals("1 customers found.",
                new SearchCustomerCommand(new CustomerSearchPredicate("Lily")).execute(model).getFeedbackToUser());
        assertEquals(List.of(LILY), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());

        assertEquals(ListDogCommand.MESSAGE_SUCCESS, new ListDogCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(cookie), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, customersOnlyResult(), expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        model.updateFilteredCustomerList(unused -> false);
        assertCommandSuccess(new ListCommand(), model, customersOnlyResult(), expectedModel);
    }

    private CommandResult customersOnlyResult() {
        return new CommandResult(ListCommand.MESSAGE_SUCCESS);
    }
}
