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
        assertEquals(new WatchDog(), new WatchDog(modelManager.getWatchDog()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new WatchDog(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    //@@author its-shoutime
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
        assertEquals(1, modelManager.getWatchDog().getCustomerList().size());
        assertEquals(1, modelManager.getWatchDog().getDogList().size());
    }

    @Test
    public void restoreState_preservesRelationshipsAndNextIds() throws Exception {
        Customer owner = new Customer(1, "Alice", "91234567", "alice@example.com", "98765432");
        Customer nextCustomer = new Customer(2, "Bob", "81234567", "bob@example.com", "87654321");
        modelManager.addCustomer(owner);
        Dog buddy = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Activity walk = modelManager.addActivity(new ActivityName("Walk"), LocalDate.now(),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        modelManager.linkDogs(owner.getId(), List.of(buddy.getId()));
        modelManager.enrolDogs(walk.getId(), walk.getStartDate(), List.of(buddy.getId()));
        Model.ModelState state = modelManager.createState();
        long nextCustomerId = state.watchDog().getNextCustomerId();
        long nextDogId = state.watchDog().getNextDogId();

        long nextActivityId = state.watchDog().getNextActivityId();

        modelManager.addCustomer(nextCustomer);
        modelManager.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("large")));
        modelManager.addActivity(new ActivityName("Run"), LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        modelManager.restoreState(state);

        ReadOnlyWatchDog restored = modelManager.getWatchDog();
        assertEquals(List.of(owner), restored.getCustomerList());
        assertEquals(List.of(walk), restored.getActivityList());
        assertEquals(List.of(new Enrolment(walk.getId(), buddy.getId(), walk.getStartDate())),
                restored.getEnrolmentList());
        assertEquals(owner.getId(), restored.getDogList().get(0).getOwnerId().orElseThrow());
        assertEquals(nextCustomerId, restored.getNextCustomerId());
        assertEquals(nextDogId, restored.getNextDogId());
        assertEquals(nextActivityId, restored.getNextActivityId());

        modelManager.addCustomer(nextCustomer);
        Dog nextDog = modelManager.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("large")));
        Activity nextActivity = modelManager.addActivity(new ActivityName("Run"), LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        assertEquals(nextCustomerId, modelManager.getWatchDog().getCustomerList().get(1).getId());
        assertEquals(nextDogId, (long) nextDog.getId());
        assertEquals(nextActivityId, nextActivity.getId());
    }
    //@@author

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
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(ALICE);
        modelManager = new ModelManager(watchDog, new UserPrefs());

        assertTrue(modelManager.hasCustomer(ALICE));
        assertFalse(modelManager.hasCustomer(BOB));
    }

    @Test
    public void getFilteredCustomerList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredCustomerList().remove(0));
    }

    // @@author XanderGoh
    @Test
    public void activityEnrolmentSummary_multipleDogsAndDates_isSortedAndGrouped() throws Exception {
        Activity activity = modelManager.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.DAILY);
        Dog first = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Dog second = modelManager.addDog(new Dog(new DogName("Max"), new Breed("Labrador"), new Size("large")));
        LocalDate firstDate = activity.getStartDate().plusDays(2);
        LocalDate secondDate = activity.getStartDate().plusDays(1);
        modelManager.enrolDogs(activity.getId(), firstDate, List.of(first.getId()));
        modelManager.enrolDogs(activity.getId(), secondDate, List.of(first.getId(), second.getId()));
        modelManager.setActivityEnrolmentDogPredicate(dog -> true);

        assertEquals("Enrolments:\n1. Buddy: 2028-03-01, 2028-03-02\n"
                + "2. Max: 2028-03-01", modelManager.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void activityEnrolmentSummary_noMatchingDogs_returnsEmpty() {
        Activity activity = modelManager.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        modelManager.setActivityEnrolmentDogPredicate(dog -> false);

        assertEquals("", modelManager.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void activityEnrolmentSummary_matchingDogWithoutEnrolment_returnsEmpty() {
        Activity activity = modelManager.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        modelManager.setActivityEnrolmentDogPredicate(dog -> true);

        assertEquals("", modelManager.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void activityEnrolmentSummary_replacedSearch_doesNotShowStaleDates() throws Exception {
        Activity activity = modelManager.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Dog first = modelManager.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Dog second = modelManager.addDog(new Dog(new DogName("Max"), new Breed("Labrador"), new Size("large")));
        modelManager.enrolDogs(activity.getId(), activity.getStartDate(), List.of(first.getId()));
        modelManager.setActivityEnrolmentDogPredicate(dog -> dog.getId() == first.getId());
        assertTrue(modelManager.getActivityEnrolmentSummary(activity).contains("Buddy"));

        modelManager.setActivityEnrolmentDogPredicate(dog -> dog.getId() == second.getId());

        assertEquals("", modelManager.getActivityEnrolmentSummary(activity));
    }
    // @@author

    @Test
    public void equals() {
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(ALICE);
        watchDog.addCustomer(BOB);
        WatchDog differentWatchDog = new WatchDog();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(watchDog, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(watchDog, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different watchDog -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentWatchDog, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredCustomerList(customer -> customer.getId() == ALICE.getId());
        assertFalse(modelManager.equals(new ModelManager(watchDog, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredCustomerList(Model.PREDICATE_SHOW_ALL_CUSTOMERS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(watchDog, differentUserPrefs)));
    }

    @Test
    public void equals_differentSearchResults_returnsFalse() {
        WatchDog watchDog = new WatchDog();
        watchDog.addCustomer(new Customer(42, "Alex", "91234567", "alex@example.com", "98765432"));
        watchDog.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        watchDog.addActivity(new ActivityName("Walk"), LocalDate.of(2026, 10, 20),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        ModelManager first = new ModelManager(watchDog, new UserPrefs());
        ModelManager second = new ModelManager(watchDog, new UserPrefs());

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

}
