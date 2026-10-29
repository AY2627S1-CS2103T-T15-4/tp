package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;

public class SearchDogCommandTest {
    private static final LocalDate ENROL_DATE = LocalDate.now().plusDays(7);

    @Test
    public void execute_invalidIds_preservesPreviousDisplay() throws Exception {
        ModelManager model = modelWithDogs();
        Activity activity = model.addActivity(new ActivityName("Walk"), ENROL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(1));
        new SearchDogCommand(null, null, null, 1).execute(model);
        var customers = List.copyOf(model.getFilteredCustomerList());
        var dogs = List.copyOf(model.getFilteredDogList());
        String summary = model.getActivityEnrolmentSummary(activity);

        for (int id : new int[]{-1, 0, 999, Integer.MAX_VALUE}) {
            assertThrows(CommandException.class, "Error in searching dogs:\nPlease enter a valid dog index", () ->
                    new SearchDogCommand(null, null, null, id).execute(model));
            assertEquals(customers, model.getFilteredCustomerList());
            assertEquals(dogs, model.getFilteredDogList());
            assertEquals(List.of(activity), model.getFilteredActivityList());
            assertEquals(summary, model.getActivityEnrolmentSummary(activity));
        }
    }

    @Test
    public void execute_combinedFields_filtersDogsAndLinkedCustomers() throws Exception {
        ModelManager model = modelWithDogs();

        CommandResult result = new SearchDogCommand("waffle", "golden retriever", "medium", 1).execute(model);

        assertEquals("Successfully searched dogs by name: waffle, breed: golden retriever, size: medium, id: 1",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_preparedPredicate_filtersDogsAndOwners() throws Exception {
        ModelManager model = modelWithDogs();

        new SearchDogCommand(new DogSearchPredicate("waffle")).execute(model);

        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_noMatchingDogs_clearsBothFilteredLists() throws Exception {
        ModelManager model = modelWithDogs();

        new SearchDogCommand("missing", null, null, null).execute(model);

        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());
        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_withoutFields_showsAllDogsAndOwners() throws Exception {
        ModelManager model = modelWithDogs();

        CommandResult result = new SearchDogCommand(null, null, null, null).execute(model);

        assertEquals("Successfully searched dogs by ", result.getFeedbackToUser());
        assertEquals(3, model.getFilteredDogList().size());
        assertEquals(1, model.getFilteredCustomerList().size());
        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_unownedDog_doesNotDisplayCustomers() throws Exception {
        ModelManager model = modelWithDogs();

        new SearchDogCommand(null, null, null, 3).execute(model);

        assertEquals(1, model.getFilteredDogList().size());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_refreshesRelatedCustomersAfterDogRelationshipChange() throws Exception {
        ModelManager model = modelWithDogs();
        new SearchDogCommand(null, "poodle", null, null).execute(model);
        model.unlinkDogs(List.of(2));

        assertEquals(2, model.getFilteredDogList().size());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_keepsNewMatchingDogAfterFilterIsActive() throws Exception {
        ModelManager model = modelWithDogs();
        new SearchDogCommand("waffle", null, null, null).execute(model);
        model.addDog(new Dog(new DogName("Waffle Puppy"), new Breed("Poodle"), new Size("Small")));

        assertEquals(2, model.getFilteredDogList().size());
    }

    @Test
    public void execute_filtersActivitiesToThoseWithMatchingDogsAndReplacesActivityFilter() throws Exception {
        ModelManager model = modelWithDogs();
        Activity matchingActivity = model.addActivity(new ActivityName("Waffle walk"), ENROL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        Activity unrelatedActivity = model.addActivity(new ActivityName("Bow Wow training"), ENROL_DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        Activity noEnrolmentActivity = model.addActivity(new ActivityName("Empty activity"), ENROL_DATE,
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        model.enrolDogs(matchingActivity.getId(), ENROL_DATE, List.of(model.getDogList().get(0).getId()));
        model.enrolDogs(unrelatedActivity.getId(), ENROL_DATE, List.of(model.getDogList().get(1).getId()));

        new SearchActivityCommand("empty", null, null).execute(model);
        new SearchDogCommand("waffle", null, null, null).execute(model);

        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
        assertEquals(List.of(matchingActivity), model.getFilteredActivityList());
    }

    @Test
    public void execute_activeFilter_refreshesActivitiesAfterEnrolmentChanges() throws Exception {
        ModelManager model = modelWithDogs();
        Activity activity = model.addActivity(new ActivityName("Waffle walk"), ENROL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);

        new SearchDogCommand("waffle", null, null, null).execute(model);
        assertTrue(model.getFilteredActivityList().isEmpty());

        int waffleId = model.getDogList().get(0).getId();
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(waffleId));
        assertEquals(List.of(activity), model.getFilteredActivityList());

        model.unenrolDogs(activity.getId(), ENROL_DATE, List.of(waffleId));
        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_dogSearchActivitySummary_groupsSortedEnrolmentDatesByDog() throws Exception {
        ModelManager model = modelWithDogs();
        Activity activity = model.addActivity(new ActivityName("Waffle walk"), ENROL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        int waffleId = model.getDogList().get(0).getId();
        model.enrolDogs(activity.getId(), ENROL_DATE.plusDays(7), List.of(waffleId));
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(waffleId));

        new SearchDogCommand("waffle", null, null, null).execute(model);

        assertEquals("Enrolments:\n1. Waffle Boy: " + ENROL_DATE + ", " + ENROL_DATE.plusDays(7),
                model.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void execute_unknownId_throwsCommandException() {
        ModelManager model = new ModelManager();
        SearchDogCommand command = new SearchDogCommand(null, null, null, 99);

        assertThrows(CommandException.class, SearchDogCommand.MESSAGE_INVALID_ID, () ->
                command.execute(model));
    }

    @Test
    public void equals_sameFields_returnsExpected() {
        SearchDogCommand command = new SearchDogCommand("waffle", "poodle", "small", 1);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new SearchDogCommand("waffle", "poodle", "small", 1)));
        assertFalse(command.equals(new SearchDogCommand("milo", "poodle", "small", 1)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("not a filter command"));
    }

    private ModelManager modelWithDogs() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna Lee", "91234567", "anna@example.com", "98765432"));
        Dog waffle = model.addDog(new Dog(new DogName("Waffle Boy"), new Breed("Golden Retriever"),
                new Size("Medium")));
        Dog bowWow = model.addDog(new Dog(new DogName("Bow Wow"), new Breed("Poodle"), new Size("Small")));
        model.addDog(new Dog(new DogName("Milo"), new Breed("Poodle"), new Size("Large")));
        model.linkDogs(1, List.of(waffle.getId(), bowWow.getId()));
        return model;
    }
}
