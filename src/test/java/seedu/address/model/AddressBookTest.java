package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getCustomerList());
        assertEquals(List.of(), addressBook.getDogList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void addDog_assignsIncreasingIds() {
        Dog firstDog = addressBook.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));
        Dog secondDog = addressBook.addDog(new Dog(new DogName("Milo"),
                new Breed("Poodle"), new Size("Small")));

        assertEquals(1, firstDog.getId());
        assertEquals(2, secondDog.getId());
        assertEquals(List.of(firstDog, secondDog), addressBook.getDogList());
    }

    @Test
    public void addDogFromStorage_preservesIdAndAdvancesCounter() {
        Dog storedDog = new Dog(7, new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large"));
        addressBook.addDogFromStorage(storedDog);

        Dog addedDog = addressBook.addDog(new Dog(new DogName("Milo"),
                new Breed("Poodle"), new Size("Small")));

        assertEquals(7, addressBook.getDogList().get(0).getId());
        assertEquals(8, addedDog.getId());
    }

    @Test
    public void hasDog_sameId_returnsTrue() {
        Dog storedDog = new Dog(3, new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large"));
        addressBook.addDogFromStorage(storedDog);

        Dog sameIdentity = new Dog(3, new DogName("Different Name"),
                new Breed("Poodle"), new Size("Small"));

        assertTrue(addressBook.hasDog(sameIdentity));
    }

    @Test
    public void getRecordLists_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getDogList().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getCustomerList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{dogs=" + addressBook.getDogList()
                + ", customers=" + addressBook.getCustomerList() + "}";
        assertEquals(expected, addressBook.toString());
    }

}
