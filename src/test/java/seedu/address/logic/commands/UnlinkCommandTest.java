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
import seedu.address.model.dog.exceptions.UnlinkException;

/** Tests atomic unlinking through the real parser and model. */
public class UnlinkCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.addCustomer(new Customer(7, "Alex", "91234567", "alex@example.com", "98765432"));
        book.addCustomer(new Customer(8, "Sam", "92345678", "sam@example.com", "98765432"));
        for (int id : List.of(42, 43, 44, 45)) {
            book.addDogFromStorage(new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        }
        book.linkDogs(7, List.of(42, 43));
        book.linkDogs(8, List.of(44));
        model = new ModelManager(book, new UserPrefs());
    }

    @Test
    public void execute_validBatch_preservesRecordsAndFiltersAndAllowsReassignment() throws Exception {
        var beforeDogs = List.copyOf(model.getDogList());
        var beforeCustomers = List.copyOf(model.getCustomerList());
        model.updateFilteredCustomerList(customer -> false);
        model.updateFilteredDogList(dog -> dog.getId() == 43);
        assertEquals("Unlinked dog IDs [42, 43] from their owners.", execute("unlink d/42 d/43"));
        assertEquals(List.of(beforeDogs.get(0).withoutOwner(), beforeDogs.get(1).withoutOwner(),
                beforeDogs.get(2), beforeDogs.get(3)), model.getDogList());
        assertEquals(beforeCustomers, model.getCustomerList());
        assertTrue(model.getFilteredCustomerList().isEmpty());
        assertEquals(List.of(model.getDogList().get(1)), model.getFilteredDogList());
        execute("link c/8 d/42 d/43");
        assertEquals(Optional.of(8), model.getDogList().get(0).getOwnerId());
        assertEquals(Optional.of(8), model.getDogList().get(1).getOwnerId());
    }

    @Test
    public void execute_editedLinkedDog_preservesOwnershipUntilExplicitDogUnlink() throws Exception {
        execute("editdog 42 n/Milo b/Beagle s/Large");
        Dog edited = model.getDogList().get(0);
        assertEquals(new Dog(42, new DogName("Milo"), new Breed("Beagle"), new Size("Large"), 7), edited);
        assertRejected("link c/8 d/42", "Dog ID 42 is already linked to customer ID 7.");

        execute("unlink d/42");
        assertEquals(edited.withoutOwner(), model.getDogList().get(0));
        assertEquals(Optional.of(7), model.getDogList().get(1).getOwnerId());
        execute("link c/8 d/42");
        assertEquals(edited.withOwner(8), model.getDogList().get(0));
    }

    @Test
    public void execute_customerAfterDogEdit_unlinksEditedAndOtherOwnedDogs() throws Exception {
        execute("editdog 42 n/Milo");
        assertEquals(Optional.of(7), model.getDogList().get(0).getOwnerId());
        Dog edited = model.getDogList().get(0);
        Dog otherOwnersDog = model.getDogList().get(2);

        execute("unlink c/7");
        assertEquals(edited.withoutOwner(), model.getDogList().get(0));
        assertTrue(model.getDogList().get(1).getOwnerId().isEmpty());
        assertEquals(otherOwnersDog, model.getDogList().get(2));
    }

    @Test
    public void execute_singleDog_leavesOtherOwnedDogLinked() throws Exception {
        execute("unlink d/42");
        assertTrue(model.getDogList().get(0).getOwnerId().isEmpty());
        assertEquals(Optional.of(7), model.getDogList().get(1).getOwnerId());
        assertRejected("unlink d/42", "Dog ID 42 has no owner.");
    }

    @Test
    public void execute_missingOrUnownedDog_rejectsEntireBatch() {
        assertRejected("unlink c/99", "Customer ID 99 does not exist.");
        assertRejected("unlink d/42 d/99", "Dog ID 99 does not exist.");
        assertRejected("unlink d/99 d/42", "Dog ID 99 does not exist.");
        assertRejected("unlink d/42 d/45", "Dog ID 45 has no owner.");
    }

    @Test
    public void unlinkDogs_emptyOrRepeatedIds_rejectsWithoutMutation() {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(UnlinkException.class, () -> model.unlinkDogs(List.of()));
        assertThrows(UnlinkException.class, () -> model.unlinkDogs(List.of(42, 42)));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void constructorAndEquals_validateAndCompareIds() {
        UnlinkCommand command = new UnlinkCommand(List.of(42, 43));
        assertEquals(command, command);
        assertEquals(command, new UnlinkCommand(List.of(42, 43)));
        assertNotEquals(command, new UnlinkCommand(8));
        assertNotEquals(command, new UnlinkCommand(List.of(42)));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
        assertThrows(IllegalArgumentException.class, () -> new UnlinkCommand(-1));
        assertThrows(IllegalArgumentException.class, () -> new UnlinkCommand(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new UnlinkCommand(List.of(-1)));
        assertThrows(IllegalArgumentException.class, () -> new UnlinkCommand(List.of(42, 42)));
    }

    @Test
    public void execute_dogsWithDifferentOwners_unlinksBoth() throws Exception {
        execute("unlink d/42 d/44");
        assertTrue(model.getDogList().get(0).getOwnerId().isEmpty());
        assertTrue(model.getDogList().get(2).getOwnerId().isEmpty());
        assertEquals(Optional.of(7), model.getDogList().get(1).getOwnerId());
    }

    @Test
    public void execute_customer_unlinksAllOwnedDogsIncludingHiddenDogs() throws Exception {
        var beforeDogs = List.copyOf(model.getDogList());
        var beforeCustomers = List.copyOf(model.getCustomerList());
        model.updateFilteredDogList(dog -> dog.getId() == 42);
        model.updateFilteredCustomerList(customer -> false);
        assertEquals("Unlinked all dogs from customer ID 7.", execute("unlink c/7"));
        assertEquals(List.of(beforeDogs.get(0).withoutOwner(), beforeDogs.get(1).withoutOwner(),
                beforeDogs.get(2), beforeDogs.get(3)), model.getDogList());
        assertEquals(beforeCustomers, model.getCustomerList());
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertTrue(model.getFilteredCustomerList().isEmpty());
        assertRejected("unlink c/7", "Customer ID 7 has no linked dogs.");
    }

    @Test
    public void equals_customerRequests_comparesCustomerIdsAndMode() {
        assertEquals(new UnlinkCommand(7), new UnlinkCommand(7));
        assertNotEquals(new UnlinkCommand(7), new UnlinkCommand(8));
        assertNotEquals(new UnlinkCommand(7), new UnlinkCommand(List.of(7)));
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
