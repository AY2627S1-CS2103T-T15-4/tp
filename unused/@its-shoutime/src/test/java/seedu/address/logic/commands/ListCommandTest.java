// Retired list-customer tests, preserved outside the active test source set.
// Unannotated portions retain their original AB3 and mixed setup provenance.
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
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
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(LILY);
        model = new ModelManager(watchDog, new UserPrefs());
        expectedModel = new ModelManager(model.getWatchDog(), new UserPrefs());
    }

    //@@author its-shoutime-unused
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

        assertEquals("Successfully searched dogs by ",
                new SearchDogCommand(new DogSearchPredicate("Cookie")).execute(model).getFeedbackToUser());
        assertEquals(List.of(cookie), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());

        assertEquals("Successfully searched customers by ",
                new SearchCustomerCommand(new CustomerSearchPredicate("Lily")).execute(model).getFeedbackToUser());
        assertEquals(List.of(LILY), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());

        assertEquals(ListDogCommand.MESSAGE_SUCCESS, new ListDogCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(cookie), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
    }

    //@@author
    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, customersOnlyResult(), expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        model.updateFilteredCustomerList(unused -> false);
        assertCommandSuccess(new ListCommand(), model, customersOnlyResult(), expectedModel);
    }

    //@@author its-shoutime-unused
    private CommandResult customersOnlyResult() {
        return new CommandResult(ListCommand.MESSAGE_SUCCESS);
    }
}
