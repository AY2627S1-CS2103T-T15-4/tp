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
public class WatchDogOwnershipTest {
    // @@author Kenneth-Chia
    private static final Customer FIRST_OWNER = new Customer(7, "Alex", "91234567",
            "alex@example.com", "98765432");
    private static final Customer SECOND_OWNER = new Customer(8, "Sam", "92345678",
            "sam@example.com", "98765432");
    // @@author
    private final WatchDog watchDog = ownedBook();

    @Test
    public void setDog_missingOwner_rejectsWithoutMutation() {
        WatchDog before = new WatchDog(watchDog);
        Dog original = watchDog.getDogList().get(0);

        assertThrows(IllegalArgumentException.class, () -> watchDog.setDog(original, original.withOwner(99)));

        assertSameState(before, watchDog);
    }

    @Test
    public void setDog_preservedOwner_allowsDetailsChange() {
        Dog original = watchDog.getDogList().get(0);
        Dog edited = new Dog(original.getId(), new DogName("Milo"), original.getBreed(), original.getSize(),
                FIRST_OWNER.getId());
        List<Dog> unaffected = List.copyOf(watchDog.getDogList().subList(1, 3));

        watchDog.setDog(original, edited);

        assertEquals(List.of(edited, unaffected.get(0), unaffected.get(1)), watchDog.getDogList());
        assertEquals(List.of(FIRST_OWNER, SECOND_OWNER), watchDog.getCustomerList());
    }

    @Test
    public void setDog_unownedReplacement_allowsRemovingOwner() {
        Dog original = watchDog.getDogList().get(0);
        Dog unowned = original.withoutOwner();
        List<Dog> unaffected = List.copyOf(watchDog.getDogList().subList(1, 3));

        watchDog.setDog(original, unowned);

        assertEquals(List.of(unowned, unaffected.get(0), unaffected.get(1)), watchDog.getDogList());
        assertEquals(List.of(FIRST_OWNER, SECOND_OWNER), watchDog.getCustomerList());
    }

    @Test
    public void setCustomers_removesReferencedOwner_rejectsWithoutMutation() {
        WatchDog before = new WatchDog(watchDog);

        assertThrows(IllegalArgumentException.class, () -> watchDog.setCustomers(List.of(SECOND_OWNER)));
        assertSameState(before, watchDog);
        assertThrows(IllegalArgumentException.class, () -> watchDog.setCustomers(List.of()));
        assertSameState(before, watchDog);
        // @@author Kenneth-Chia
    }

    @Test
    public void setCustomers_preservesOwnerIdentities_allowsDetailsChange() {
        Customer renamed = new Customer(FIRST_OWNER.getId(), "Alex Tan", "91234567",
                "alex@example.com", "98765432");
        // @@author
        List<Dog> beforeDogs = List.copyOf(watchDog.getDogList());

        watchDog.setCustomers(List.of(renamed, SECOND_OWNER));

        assertEquals(List.of(renamed, SECOND_OWNER), watchDog.getCustomerList());
        assertEquals(beforeDogs, watchDog.getDogList());
    }

    @Test
    public void resetData_danglingOwner_rejectsBeforeReplacingRecordsOrCounters() {
        WatchDog before = new WatchDog(watchDog);
        ReadOnlyWatchDog invalid = new ReadOnlyWatchDog() {
            // @@author Kenneth-Chia
            @Override
            public ObservableList<Customer> getCustomerList() {
                return FXCollections.observableArrayList(SECOND_OWNER);
            }

            @Override
            public ObservableList<Dog> getDogList() {
                return FXCollections.observableArrayList(dog(52).withOwner(99));
            }

            @Override
            // @@author
            public long getNextDogId() {
                return 100;
            }

            @Override
            public long getNextCustomerId() {
                return 200;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> watchDog.resetData(invalid));

        assertSameState(before, watchDog);
    }

    @Test
    public void resetData_validNewOwnership_validatesAgainstReplacementCustomers() {
        WatchDog replacement = new WatchDog();
        // @@author Kenneth-Chia
        Customer newOwner = new Customer(9, "Beth", "93456789", "beth@example.com", "98765432");
        replacement.addCustomer(newOwner);
        replacement.addDogFromStorage(dog(52).withOwner(newOwner.getId()));
        replacement.addDogFromStorage(dog(53));
        replacement.setNextDogId(100);
        replacement.setNextCustomerId(200);
        // @@author

        watchDog.resetData(replacement);

        assertSameState(replacement, watchDog);
    }

    @Test
    public void resetData_self_preservesOwnershipAndCounters() {
        WatchDog before = new WatchDog(watchDog);

        watchDog.resetData(watchDog);

        assertSameState(before, watchDog);
    }

    @Test
    public void linkAndUnlink_positiveDogIds_preserveRecordsAndCounters() throws Exception {

        WatchDog book = new WatchDog();
        Customer firstOwner = new Customer(1, "Alex", "91234567", "alex@example.com", "98765432");
        Customer largeOwner = new Customer(128, "Sam", "92345678", "sam@example.com", "98765432");
        book.addCustomer(firstOwner);
        book.addCustomer(largeOwner);
        book.addDogFromStorage(dog(1));
        book.addDogFromStorage(dog(127));
        book.addDogFromStorage(dog(128));
        long nextDogId = book.getNextDogId();
        long nextCustomerId = book.getNextCustomerId();

        book.linkDogs(1, List.of(1));
        book.linkDogs(128, List.of(127, 128));

        assertEquals(List.of(dog(1).withOwner(1), dog(127).withOwner(128), dog(128).withOwner(128)),

                book.getDogList());
        book.unlinkDogs(List.of(1, 128));
        assertEquals(List.of(dog(1), dog(127).withOwner(128), dog(128)), book.getDogList());
        book.unlinkCustomerDogs(128);
        assertEquals(List.of(dog(1), dog(127), dog(128)), book.getDogList());
        assertEquals(List.of(firstOwner, largeOwner), book.getCustomerList());

        // @@author Kenneth-Chia
        assertEquals(nextDogId, book.getNextDogId());
        assertEquals(nextCustomerId, book.getNextCustomerId());
    }

    @Test
    public void linkDogs_nullEntry_rejectsEntireBatchWithoutMutation() {
        // @@author
        WatchDog before = new WatchDog(watchDog);

        assertThrows(LinkException.class, () -> watchDog.linkDogs(7, Arrays.asList(44, null)));

        assertSameState(before, watchDog);
    }

    @Test
    public void linkAndUnlink_zeroDogId_rejectWithoutMutation() {
        WatchDog before = new WatchDog(watchDog);

        assertThrows(LinkException.class, "Dog IDs must be positive.", () ->
                watchDog.linkDogs(FIRST_OWNER.getId(), List.of(0)));
        assertThrows(UnlinkException.class, "Dog IDs must be positive.", () ->
                watchDog.unlinkDogs(List.of(0)));

        assertSameState(before, watchDog);
    }

    @Test
    public void unlinkDogs_nullEntry_rejectsEntireBatchWithoutMutation() {
        WatchDog before = new WatchDog(watchDog);

        assertThrows(UnlinkException.class, () -> watchDog.unlinkDogs(Arrays.asList(42, null)));

        assertSameState(before, watchDog);
    }

    private static WatchDog ownedBook() {
        WatchDog book = new WatchDog();
        // @@author Kenneth-Chia
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
    // @@author

    private static void assertSameState(WatchDog expected, WatchDog actual) {
        // @@author Kenneth-Chia
        assertEquals(expected, actual);
        assertEquals(expected.getNextDogId(), actual.getNextDogId());
        assertEquals(expected.getNextCustomerId(), actual.getNextCustomerId());
        assertEquals(expected.getNextActivityId(), actual.getNextActivityId());
    }
}
// @@author
