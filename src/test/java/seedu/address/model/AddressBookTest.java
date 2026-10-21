package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.ALICE;
import static seedu.address.testutil.TypicalWatchDog.MAX;
import static seedu.address.testutil.TypicalWatchDog.getTypicalAddressBook;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
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
    public void setActivity_invalidIdentityOrStaleTarget_rejectsWithoutChangingActivity() {
        Activity original = addressBook.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        Activity differentId = new Activity(2, original.getName(), original.getStartDate(),
                original.getStartTime(), original.getEndTime(), original.getRecurrence());
        Activity staleTarget = original.withName(new ActivityName("Old name"));
        Activity renamed = original.withName(new ActivityName("Training"));

        assertThrows(IllegalArgumentException.class, () -> addressBook.setActivity(original, differentId));
        assertThrows(IllegalArgumentException.class, () -> addressBook.setActivity(staleTarget, renamed));
        assertEquals(List.of(original), addressBook.getActivityList());
    }

    @Test
    public void setActivity_nonexistentTarget_rejectsWithoutChangingActivity() {
        Activity original = addressBook.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        Activity missing = new Activity(2, new ActivityName("Training"), LocalDate.of(2028, 3, 1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);

        assertThrows(IllegalArgumentException.class, () -> addressBook.setActivity(missing, missing));
        assertEquals(List.of(original), addressBook.getActivityList());
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
    public void equals_sameRecords_returnsTrue() {
        AddressBook original = getTypicalAddressBook();
        AddressBook copy = new AddressBook(original);

        assertEquals(original, original);
        assertEquals(original, copy);
        assertEquals(copy, original);
        assertEquals(new AddressBook(), new AddressBook());
    }

    @Test
    public void equals_differentDogs_returnsFalse() {
        AddressBook original = getTypicalAddressBook();
        AddressBook differentDogs = new AddressBook(original);
        differentDogs.removeDog(MAX);

        assertEquals(original.getCustomerList(), differentDogs.getCustomerList());
        assertNotEquals(original, differentDogs);
    }

    @Test
    public void equals_differentCustomers_returnsFalse() {
        AddressBook original = getTypicalAddressBook();
        AddressBook differentCustomers = new AddressBook(original);
        differentCustomers.setCustomers(List.of(ALICE));

        assertEquals(original.getDogList(), differentCustomers.getDogList());
        assertNotEquals(original, differentCustomers);
    }

    @Test
    public void equals_nullOrDifferentType_returnsFalse() {
        assertFalse(addressBook.equals(null));
        assertFalse(addressBook.equals("address book"));
    }

    @Test
    public void hashCode_equalBooks_returnsSameHashCode() {
        AddressBook original = getTypicalAddressBook();
        AddressBook copy = new AddressBook(original);

        assertEquals(original.hashCode(), original.hashCode());
        assertEquals(original.hashCode(), copy.hashCode());
        assertEquals(new AddressBook().hashCode(), new AddressBook().hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{dogs=" + addressBook.getDogList()
                + ", customers=" + addressBook.getCustomerList() + "}";
        assertEquals(expected, addressBook.toString());
    }

}
