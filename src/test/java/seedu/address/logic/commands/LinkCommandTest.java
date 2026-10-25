package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.model.dog.exceptions.LinkException;

/** Tests batch ownership changes through the real parser and model. */
public class LinkCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        AddressBook book = new AddressBook();
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
        assertEquals("Linked dog IDs [42, 43] to customer ID 7.", execute("link c/7 d/42 d/43"));
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
        assertRejected("link c/7 d/42 d/99", "Dog ID 99 does not exist.");
        assertRejected("link c/7 d/99 d/42", "Dog ID 99 does not exist.");
    }

    @Test
    public void execute_alreadyOwned_rejectsSameOwnerAndReassignmentAtomically() throws Exception {
        execute("link c/7 d/43");
        assertRejected("link c/7 d/42 d/43", "Dog ID 43 is already linked to customer ID 7.");
        assertRejected("link c/8 d/42 d/43", "Dog ID 43 is already linked to customer ID 7.");
    }

    @Test
    public void deleteCustomer_linkedDogs_unlinksOnlyOwnedDogsAndPreservesRecords() throws Exception {
        Dog firstUnowned = model.getDogList().get(0);
        Dog secondUnowned = model.getDogList().get(1);
        execute("link c/7 d/42 d/43");
        execute("link c/8 d/44");
        Dog otherOwnersDog = model.getDogList().get(2);
        Dog unlinked = model.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("small")));
        model.updateFilteredDogList(dog -> false);
        execute("delete customer 7");
        assertEquals(List.of(firstUnowned, secondUnowned, otherOwnersDog, unlinked), model.getDogList());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertEquals(8, model.getCustomerList().get(0).getId());
        assertEquals(1, model.getCustomerList().size());
        execute("link c/8 d/42 d/43");
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
        AddressBook before = new AddressBook(model.getAddressBook());
        Customer differentId = new Customer(9, "Alex Tan", "91234567", "alex@example.com", "98765432");
        assertThrows(IllegalArgumentException.class, () -> model.setCustomer(edited, differentId));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void linkDogs_directInvalidBatch_preservesModel() {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(LinkException.class, () -> model.linkDogs(7, List.of()));
        assertThrows(LinkException.class, () -> model.linkDogs(7, List.of(42, 42)));
        assertEquals(before, model.getAddressBook());
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
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of(-1)));
        assertThrows(IllegalArgumentException.class, () -> new LinkCommand(7, List.of(42, 42)));
    }

    private void assertRejected(String input, String message) {
        AddressBook before = new AddressBook(model.getAddressBook());
        var visibleDogs = List.copyOf(model.getFilteredDogList());
        var visibleCustomers = List.copyOf(model.getFilteredCustomerList());
        CommandException exception = assertThrows(CommandException.class, () -> execute(input));
        assertEquals(message, exception.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(visibleDogs, model.getFilteredDogList());
        assertEquals(visibleCustomers, model.getFilteredCustomerList());
    }

    private String execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }
}
