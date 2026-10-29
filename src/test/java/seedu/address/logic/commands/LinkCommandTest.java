package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.WatchDogParser;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.model.dog.exceptions.LinkException;

/** Tests batch ownership changes through the real parser and model. */
public class LinkCommandTest {
    private final WatchDogParser parser = new WatchDogParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        WatchDog book = new WatchDog();
        book.addCustomer(new Customer(7, "Alex", "91234567", "alex@example.com", "98765432"));
        book.addCustomer(new Customer(8, "Sam", "92345678", "sam@example.com", "98765432"));
        for (int id : List.of(42, 43, 44)) {
            book.addDogFromStorage(new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        }
        model = new ModelManager(book, new UserPrefs());
    }

    @Test
    public void execute_validBatch_linksHiddenPersistentIdsAndPreservesFilters() throws Exception {
        model.updateFilteredCustomerList(customer -> false);
        model.updateFilteredDogList(dog -> dog.getId() == 43);
        var customersBefore = List.copyOf(model.getCustomerList());
        Dog unaffected = model.getDogList().get(2);
        assertEquals("Linked dog IDs [42, 43] to customer ID 7.", execute("link c/7 d/42 43"));
        assertEquals(Optional.of(7), model.getDogList().get(0).getOwnerId());
        assertEquals(Optional.of(7), model.getDogList().get(1).getOwnerId());
        assertEquals(unaffected, model.getDogList().get(2));
        assertEquals(customersBefore, model.getCustomerList());
        assertTrue(model.getFilteredCustomerList().isEmpty());
        assertEquals(List.of(model.getDogList().get(1)), model.getFilteredDogList());
    }

    @Test
    public void execute_missingEndpoint_rejectsEntireBatch() {
        assertRejected("link c/99 d/42", "Customer ID 99 does not exist.");
        assertRejected("link c/7 d/42 99", "Dog ID 99 does not exist.");
        assertRejected("link c/7 d/99 42", "Dog ID 99 does not exist.");
    }

    @Test
    public void execute_alreadyOwned_rejectsSameOwnerAndReassignmentAtomically() throws Exception {
        execute("link c/7 d/43");
        assertRejected("link c/7 d/42 43", "Dog ID 43 is already linked to customer ID 7.");
        assertRejected("link c/8 d/42 43", "Dog ID 43 is already linked to customer ID 7.");
    }

    @Test
    public void deleteCustomer_linkedDogs_unlinksOnlyOwnedDogsAndPreservesRecords() throws Exception {
        Dog firstUnowned = model.getDogList().get(0);
        Dog secondUnowned = model.getDogList().get(1);
        execute("link c/7 d/42 43");
        execute("link c/8 d/44");
        Dog otherOwnersDog = model.getDogList().get(2);
        Dog unlinked = model.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("small")));
        model.updateFilteredDogList(dog -> false);
        execute("deletecustomer 7");
        assertEquals(List.of(firstUnowned, secondUnowned, otherOwnersDog, unlinked), model.getDogList());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertEquals(8, model.getCustomerList().get(0).getId());
        assertEquals(1, model.getCustomerList().size());
        execute("link c/8 d/42 43");
        assertEquals(Optional.of(8), model.getDogList().get(0).getOwnerId());
        assertEquals(Optional.of(8), model.getDogList().get(1).getOwnerId());
    }

    @Test
    public void deleteDog_lastOwnedDog_keepsCustomer() throws Exception {
        execute("link c/7 d/42");
        model.deleteDog(model.getDogList().get(0));
        assertEquals(2, model.getCustomerList().size());
        assertEquals(List.of(43, 44), model.getDogList().stream().map(Dog::getId).toList());
    }

    @Test
    public void editCustomer_preservesOwnershipAndRejectsIdentityChange() throws Exception {
        execute("link c/7 d/42");
        Customer original = model.getCustomerList().get(0);
        Customer edited = new Customer(7, "Alex Tan", "91234567", "alex@example.com", "98765432");
        model.setCustomer(original, edited);
        assertEquals(Optional.of(7), model.getDogList().get(0).getOwnerId());
        assertEquals(edited, model.getCustomerList().get(0));
        WatchDog before = new WatchDog(model.getWatchDog());
        Customer differentId = new Customer(9, "Alex Tan", "91234567", "alex@example.com", "98765432");
        assertThrows(IllegalArgumentException.class, () -> model.setCustomer(edited, differentId));
        assertEquals(before, model.getWatchDog());
    }

    @Test
    public void linkDogs_directInvalidBatch_preservesModel() {
        WatchDog before = new WatchDog(model.getWatchDog());
        assertThrows(LinkException.class, () -> model.linkDogs(7, List.of()));
        assertThrows(LinkException.class, () -> model.linkDogs(7, List.of(42, 42)));
        assertEquals(before, model.getWatchDog());
    }

    @Test
    public void constructorAndEquals_validateAndCompareIds() {
        LinkCommand command = new LinkCommand(7, List.of(42, 43));
        assertEquals(command, command);
        assertEquals(command, new LinkCommand(7, List.of(42, 43)));
        assertEquals(command.hashCode(), new LinkCommand(7, List.of(42, 43)).hashCode());
        assertNotEquals(command, new LinkCommand(8, List.of(42, 43)));
        assertNotEquals(command, new LinkCommand(7, List.of(42)));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(-1, List.of(42)));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(0, List.of(42)));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of(-1)));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of(42, 42)));
    }

    @Test
    public void constructor_mutableInput_keepsOriginalBatch() throws Exception {
        List<Integer> ids = new ArrayList<>(List.of(42));
        LinkCommand command = new LinkCommand(7, ids);
        ids.clear();
        ids.add(44);
        Dog unaffected = model.getDogList().get(2);
        command.execute(model);
        assertEquals(Optional.of(7), model.getDogList().get(0).getOwnerId());
        assertEquals(unaffected, model.getDogList().get(2));
    }

    @Test
    public void constructor_nullAndZeroInputs_rejectedIndependently() {
        assertThrows(NullPointerException.class, () -> new LinkCommand(7, null));
        assertThrows(NullPointerException.class, () -> new LinkCommand(7, Arrays.asList(42, null)));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of(0)));
    }

    @Test
    public void model_invalidIds_rejectsEachBatchWithoutMutation() {
        WatchDog before = new WatchDog(model.getWatchDog());
        assertThrows(NullPointerException.class, () -> model.linkDogs(7, null));
        for (List<Integer> ids : List.of(List.of(0), List.of(-1), List.of(Integer.MIN_VALUE),
                Arrays.asList(42, null))) {
            assertThrows(LinkException.class, () -> model.linkDogs(7, ids));
            assertEquals(before, model.getWatchDog());
        }
    }

    private void assertRejected(String input, String message) {
        WatchDog before = new WatchDog(model.getWatchDog());
        var visibleDogs = List.copyOf(model.getFilteredDogList());
        var visibleCustomers = List.copyOf(model.getFilteredCustomerList());
        CommandException exception = assertThrows(CommandException.class, () -> execute(input));
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getCause() instanceof LinkException);
        assertEquals(before, model.getWatchDog());
        assertEquals(visibleDogs, model.getFilteredDogList());
        assertEquals(visibleCustomers, model.getFilteredCustomerList());
    }

    private String execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }
}
