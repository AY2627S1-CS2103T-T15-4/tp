package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;

/** Tests ownership validation at shared model boundaries. */
public class AddressBookOwnershipTest {
    private static final Customer FIRST_OWNER = new Customer(7, "Alex", "91234567",
            "alex@example.com", "98765432");
    private static final Customer SECOND_OWNER = new Customer(8, "Sam", "92345678",
            "sam@example.com", "98765432");
    private final AddressBook addressBook = ownedBook();

    @Test
    public void setDog_missingOwner_rejectsWithoutMutation() {
        AddressBook before = new AddressBook(addressBook);
        Dog original = addressBook.getDogList().get(0);

        assertThrows(IllegalArgumentException.class, () -> addressBook.setDog(original, original.withOwner(99)));

        assertSameState(before, addressBook);
    }

    @Test
    public void setDog_preservedOwner_allowsDetailsChange() {
        Dog original = addressBook.getDogList().get(0);
        Dog edited = new Dog(original.getId(), new DogName("Milo"), original.getBreed(), original.getSize(),
                FIRST_OWNER.getId());
        List<Dog> unaffected = List.copyOf(addressBook.getDogList().subList(1, 3));

        addressBook.setDog(original, edited);

        assertEquals(List.of(edited, unaffected.get(0), unaffected.get(1)), addressBook.getDogList());
        assertEquals(List.of(FIRST_OWNER, SECOND_OWNER), addressBook.getCustomerList());
    }

    @Test
    public void setDog_unownedReplacement_allowsRemovingOwner() {
        Dog original = addressBook.getDogList().get(0);
        Dog unowned = original.withoutOwner();
        List<Dog> unaffected = List.copyOf(addressBook.getDogList().subList(1, 3));

        addressBook.setDog(original, unowned);

        assertEquals(List.of(unowned, unaffected.get(0), unaffected.get(1)), addressBook.getDogList());
        assertEquals(List.of(FIRST_OWNER, SECOND_OWNER), addressBook.getCustomerList());
    }

    @Test
    public void setCustomers_removesReferencedOwner_rejectsWithoutMutation() {
        AddressBook before = new AddressBook(addressBook);

        assertThrows(IllegalArgumentException.class, () -> addressBook.setCustomers(List.of(SECOND_OWNER)));
        assertSameState(before, addressBook);
        assertThrows(IllegalArgumentException.class, () -> addressBook.setCustomers(List.of()));
        assertSameState(before, addressBook);
    }

    @Test
    public void setCustomers_preservesOwnerIdentities_allowsDetailsChange() {
        Customer renamed = new Customer(FIRST_OWNER.getId(), "Alex Tan", "91234567",
                "alex@example.com", "98765432");
        List<Dog> beforeDogs = List.copyOf(addressBook.getDogList());

        addressBook.setCustomers(List.of(renamed, SECOND_OWNER));

        assertEquals(List.of(renamed, SECOND_OWNER), addressBook.getCustomerList());
        assertEquals(beforeDogs, addressBook.getDogList());
    }

    @Test
    public void resetData_danglingOwner_rejectsBeforeReplacingRecordsOrCounters() {
        AddressBook before = new AddressBook(addressBook);
        ReadOnlyAddressBook invalid = new ReadOnlyAddressBook() {
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.observableArrayList(SECOND_OWNER);
            }

            @Override
            public ObservableList<Dog> getDogList() {
                return FXCollections.observableArrayList(dog(52).withOwner(99));
            }

            @Override
            public long getNextDogId() {
                return 100;
            }

            @Override
            public int getNextCustomerId() {
                return 200;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> addressBook.resetData(invalid));

        assertSameState(before, addressBook);
    }

    @Test
    public void resetData_validNewOwnership_validatesAgainstReplacementCustomers() {
        AddressBook replacement = new AddressBook();
        Customer newOwner = new Customer(9, "Beth", "93456789", "beth@example.com", "98765432");
        replacement.addCustomer(newOwner);
        replacement.addDogFromStorage(dog(52).withOwner(newOwner.getId()));
        replacement.addDogFromStorage(dog(53));
        replacement.setNextDogId(100);
        replacement.setNextCustomerId(200);

        addressBook.resetData(replacement);

        assertSameState(replacement, addressBook);
    }

    @Test
    public void resetData_self_preservesOwnershipAndCounters() {
        AddressBook before = new AddressBook(addressBook);

        addressBook.resetData(addressBook);

        assertSameState(before, addressBook);
    }

    @Test
    public void linkAndUnlink_positiveDogIdsAndZeroCustomerId_preserveRecordsAndCounters() throws Exception {
        AddressBook book = new AddressBook();
        Customer zeroOwner = new Customer(0, "Alex", "91234567", "alex@example.com", "98765432");
        Customer largeOwner = new Customer(128, "Sam", "92345678", "sam@example.com", "98765432");
        book.addCustomer(zeroOwner);
        book.addCustomer(largeOwner);
        book.addDogFromStorage(dog(1));
        book.addDogFromStorage(dog(127));
        book.addDogFromStorage(dog(128));
        long nextDogId = book.getNextDogId();
        int nextCustomerId = book.getNextCustomerId();

        book.linkDogs(0, List.of(1));
        book.linkDogs(128, List.of(127, 128));

        assertEquals(List.of(dog(1).withOwner(0), dog(127).withOwner(128), dog(128).withOwner(128)),
                book.getDogList());
        book.unlinkDogs(List.of(1, 128));
        assertEquals(List.of(dog(1), dog(127).withOwner(128), dog(128)), book.getDogList());
        book.unlinkCustomerDogs(128);
        assertEquals(List.of(dog(1), dog(127), dog(128)), book.getDogList());
        assertEquals(List.of(zeroOwner, largeOwner), book.getCustomerList());
        assertEquals(nextDogId, book.getNextDogId());
        assertEquals(nextCustomerId, book.getNextCustomerId());
    }

    @Test
    public void linkDogs_nullEntry_rejectsEntireBatchWithoutMutation() {
        AddressBook before = new AddressBook(addressBook);

        assertThrows(LinkException.class, () -> addressBook.linkDogs(7, Arrays.asList(44, null)));

        assertSameState(before, addressBook);
    }

    @Test
    public void linkAndUnlink_zeroDogId_rejectWithoutMutation() {
        AddressBook before = new AddressBook(addressBook);

        assertThrows(LinkException.class, "Dog IDs must be positive.", () ->
                addressBook.linkDogs(FIRST_OWNER.getId(), List.of(0)));
        assertThrows(UnlinkException.class, "Dog IDs must be positive.", () ->
                addressBook.unlinkDogs(List.of(0)));

        assertSameState(before, addressBook);
    }

    @Test
    public void unlinkDogs_nullEntry_rejectsEntireBatchWithoutMutation() {
        AddressBook before = new AddressBook(addressBook);

        assertThrows(UnlinkException.class, () -> addressBook.unlinkDogs(Arrays.asList(42, null)));

        assertSameState(before, addressBook);
    }

    private static AddressBook ownedBook() {
        AddressBook book = new AddressBook();
        book.addCustomer(FIRST_OWNER);
        book.addCustomer(SECOND_OWNER);
        book.addDogFromStorage(dog(42).withOwner(FIRST_OWNER.getId()));
        book.addDogFromStorage(dog(43).withOwner(SECOND_OWNER.getId()));
        book.addDogFromStorage(dog(44));
        book.setNextDogId(60);
        book.setNextCustomerId(70);
        return book;
    }

    private static Dog dog(int id) {
        return new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    }

    private static void assertSameState(AddressBook expected, AddressBook actual) {
        assertEquals(expected, actual);
        assertEquals(expected.getNextDogId(), actual.getNextDogId());
        assertEquals(expected.getNextCustomerId(), actual.getNextCustomerId());
        assertEquals(expected.getNextActivityId(), actual.getNextActivityId());
    }
}
