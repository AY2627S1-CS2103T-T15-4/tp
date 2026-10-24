package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.ALICE;
import static seedu.address.testutil.TypicalWatchDog.BOB;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void restoreState_restoresDataAndActiveFilters() {
        modelManager.addCustomer(ALICE);
        Dog buddy = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Activity walk = modelManager.addActivity(new ActivityName("Walk"), LocalDate.now(),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        modelManager.updateFilteredCustomerList(customer -> customer.equals(ALICE));
        modelManager.updateFilteredDogList(dog -> dog.equals(buddy));
        modelManager.updateFilteredActivityList(activity -> activity.equals(walk));
        Model.ModelState state = modelManager.createState();

        modelManager.addCustomer(BOB);
        modelManager.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("large")));
        modelManager.updateFilteredCustomerList(customer -> false);
        modelManager.updateFilteredDogList(dog -> false);
        modelManager.updateFilteredActivityList(activity -> false);
        modelManager.restoreState(state);

        assertEquals(List.of(ALICE), modelManager.getFilteredCustomerList());
        assertEquals(List.of(buddy), modelManager.getFilteredDogList());
        assertEquals(List.of(walk), modelManager.getFilteredActivityList());
        assertEquals(1, modelManager.getAddressBook().getCustomerList().size());
        assertEquals(1, modelManager.getAddressBook().getDogList().size());
    }

    @Test
    public void restoreState_restoresSelectedRecord() {
        modelManager.addCustomer(ALICE);
        Dog buddy = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        modelManager.selectDog(buddy.getId());
        Model.ModelState selectedDogState = modelManager.createState();
        modelManager.selectCustomer(ALICE.getId());
        Model.ModelState selectedCustomerState = modelManager.createState();

        modelManager.restoreState(selectedDogState);
        assertEquals(List.of(buddy), modelManager.getFilteredDogList());
        assertEquals(List.of(), modelManager.getFilteredCustomerList());

        modelManager.restoreState(selectedCustomerState);
        assertEquals(List.of(ALICE), modelManager.getFilteredCustomerList());
        assertEquals(List.of(), modelManager.getFilteredDogList());
        assertThrows(NullPointerException.class, () -> modelManager.restoreState(null));
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasCustomer_checksCustomerIdentity() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(ALICE);
        modelManager = new ModelManager(addressBook, new UserPrefs());

        assertTrue(modelManager.hasCustomer(ALICE));
        assertFalse(modelManager.hasCustomer(BOB));
    }

    @Test
    public void getFilteredCustomerList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredCustomerList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(ALICE);
        addressBook.addCustomer(BOB);
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredCustomerList(customer -> customer.getId() == ALICE.getId());
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredCustomerList(Model.PREDICATE_SHOW_ALL_CUSTOMERS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void equals_differentSearchResults_returnsFalse() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(new Customer(42, "Alex", "91234567", "alex@example.com", "98765432"));
        addressBook.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        addressBook.addActivity(new ActivityName("Walk"), LocalDate.of(2026, 10, 20),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        ModelManager first = new ModelManager(addressBook, new UserPrefs());
        ModelManager second = new ModelManager(addressBook, new UserPrefs());

        assertEquals(first, second);
        first.updateFilteredCustomerList(customer -> false);
        assertFalse(first.equals(second));
        first.updateFilteredCustomerList(customer -> true);
        first.updateFilteredDogList(dog -> false);
        assertFalse(first.equals(second));
        first.updateFilteredDogList(dog -> true);
        assertEquals(first, second);
        first.updateFilteredActivityList(activity -> false);
        assertFalse(first.equals(second));
        first.updateFilteredActivityList(Model.PREDICATE_SHOW_ALL_ACTIVITIES);
        assertEquals(first, second);
    }

    @Test
    public void selectDog_unownedDog_refreshesAfterAddingCustomer() {
        AddressBook addressBook = new AddressBook();
        addressBook.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        addressBook.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large")));
        ModelManager first = new ModelManager(addressBook, new UserPrefs());
        ModelManager second = new ModelManager(addressBook, new UserPrefs());

        first.selectDog(42);
        first.addCustomer(ALICE);

        assertFalse(first.equals(second));
        assertTrue(first.getFilteredCustomerList().isEmpty());
        assertEquals(1, first.getFilteredDogList().size());
    }

    @Test
    public void selectDog_showsOnlyItsTodayAndFutureEnrolments() {
        AddressBook addressBook = new AddressBook();
        LocalDate today = LocalDate.now();
        Dog selectedDog = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
        Dog otherDog = new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large"));
        addressBook.addDogFromStorage(selectedDog);
        addressBook.addDogFromStorage(otherDog);
        Activity pastActivity = addressBook.addActivity(new ActivityName("Past"), today.minusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity todayActivity = addressBook.addActivity(new ActivityName("Today"), today,
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        Activity futureActivity = addressBook.addActivity(new ActivityName("Future"), today.plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity otherDogActivity = addressBook.addActivity(new ActivityName("Other dog"), today,
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        addressBook.addEnrolmentFromStorage(new Enrolment(pastActivity.getId(), selectedDog.getId(),
                today.minusDays(1)));
        addressBook.addEnrolmentFromStorage(new Enrolment(todayActivity.getId(), selectedDog.getId(), today));
        addressBook.addEnrolmentFromStorage(new Enrolment(futureActivity.getId(), selectedDog.getId(),
                today.plusDays(1)));
        addressBook.addEnrolmentFromStorage(new Enrolment(otherDogActivity.getId(), otherDog.getId(), today));
        modelManager = new ModelManager(addressBook, new UserPrefs());

        modelManager.selectDog(selectedDog.getId());

        assertEquals(List.of(todayActivity, futureActivity), modelManager.getFilteredActivityList());
    }

    @Test
    public void equals_differentSelectedDog_returnsFalse() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(new Customer(7, "Alex", "91234567", "alex@example.com", "98765432"));
        addressBook.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large")));
        addressBook.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        ModelManager first = new ModelManager(addressBook, new UserPrefs());
        ModelManager second = new ModelManager(addressBook, new UserPrefs());

        first.selectDog(42);
        second.selectDog(99);

        assertFalse(first.equals(second));

        ModelManager sameFiltersFirst = new ModelManager();
        ModelManager sameFiltersSecond = new ModelManager();
        sameFiltersFirst.selectDog(2);
        sameFiltersSecond.selectDog(3);
        assertFalse(sameFiltersFirst.equals(sameFiltersSecond));
        assertThrows(AssertionError.class, () -> first.selectDog(0));
    }

    @Test
    public void equals_differentSelectedCustomer_returnsFalse() {
        AddressBook addressBook = new AddressBook();
        addressBook.addCustomer(new Customer(42, "Alex", "91234567", "alex@example.com", "98765432"));
        addressBook.addCustomer(new Customer(43, "Bob", "81234567", "bob@example.com", "87654321"));
        ModelManager first = new ModelManager(addressBook, new UserPrefs());
        ModelManager second = new ModelManager(addressBook, new UserPrefs());

        first.selectCustomer(42);
        second.selectCustomer(43);

        assertFalse(first.equals(second));

        ModelManager sameFiltersFirst = new ModelManager();
        ModelManager sameFiltersSecond = new ModelManager();
        sameFiltersFirst.selectCustomer(2);
        sameFiltersSecond.selectCustomer(3);
        assertFalse(sameFiltersFirst.equals(sameFiltersSecond));
        assertThrows(AssertionError.class, () -> first.selectCustomer(0));
    }
}
