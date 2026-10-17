package seedu.address.testutil;

import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Shared customer/dog records, including ownership and nonsequential persistent IDs. */
public class TypicalWatchDog {
    public static final Customer ALICE = new Customer(7, "Alice Tan", "91234567", "alice@example.com", "98765432");
    public static final Customer BOB = new Customer(12, "Bob Lim", "92345678", "bob@example.com", "93456789");
    public static final Dog BUDDY = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    public static final Dog MAX = new Dog(51, new DogName("Max"), new Breed("Labrador"), new Size("large"));

    /** Returns a fresh book with one linked dog and one unlinked dog. */
    public static AddressBook getTypicalAddressBook() {
        AddressBook book = new AddressBook();
        book.addCustomer(ALICE);
        book.addCustomer(BOB);
        book.addDogFromStorage(BUDDY);
        book.addDogFromStorage(MAX);
        try {
            book.linkDogs(ALICE.getId(), List.of(BUDDY.getId()));
        } catch (seedu.address.model.dog.exceptions.LinkException e) {
            throw new AssertionError(e);
        }
        return book;
    }
}
