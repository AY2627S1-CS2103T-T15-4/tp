package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class SearchActivityCommandTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);
    private static final LocalDate ENROL_DATE = LocalDate.now().plusDays(7);

    @Test
    public void execute_combinedFields_filtersWithAndLogic() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new SearchActivityCommand("dog run", DATE, LocalTime.of(16, 0)).execute(model);

        assertEquals("Successfully searched activities by name: dog run, time: 16:00, date: 2026-10-20",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(0)), model.getFilteredActivityList());
    }

    @Test
    public void execute_dateAndTime_handleRecurrenceAndInclusiveBoundaries() throws Exception {
        ModelManager model = modelWithActivities();

        new SearchActivityCommand(null, DATE.plusDays(7), LocalTime.of(8, 30)).execute(model);

        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
        CommandResult timeResult = new SearchActivityCommand(null, null, LocalTime.of(8, 30)).execute(model);
        assertEquals("Successfully searched activities by time: 08:30", timeResult.getFeedbackToUser());

        CommandResult dateResult = new SearchActivityCommand(null, DATE, null).execute(model);
        assertEquals("Successfully searched activities by date: 2026-10-20", dateResult.getFeedbackToUser());

        new SearchActivityCommand(null, DATE, LocalTime.of(16, 30)).execute(model);
        new SearchActivityCommand(null, DATE, LocalTime.of(17, 0)).execute(model);
        assertFalse(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_noMatches_returnsEmptyFilteredList() throws Exception {
        ModelManager model = modelWithActivities();

        new SearchActivityCommand("unknown", null, null).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_nameMatching_isCaseInsensitive() throws Exception {
        ModelManager model = modelWithActivities();

        new SearchActivityCommand("RUN", null, null).execute(model);

        assertEquals(List.of(model.getActivityList().get(0)), model.getFilteredActivityList());
    }

    @Test
    public void execute_matchingActivities_showsEnrolledDogsAndOwners() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        model.addCustomer(new Customer(2, "Beth", "92345678", "beth@example.com", "97654321"));
        Dog firstDog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Dog secondDog = model.addDog(new Dog(new DogName("Max"), new Breed("Labrador"), new Size("Large")));
        Dog unrelatedDog = model.addDog(new Dog(new DogName("Luna"), new Breed("Beagle"), new Size("Medium")));
        model.addDog(new Dog(new DogName("Unowned"), new Breed("Beagle"), new Size("Small")));
        model.linkDogs(1, List.of(firstDog.getId(), secondDog.getId()));
        model.linkDogs(2, List.of(unrelatedDog.getId()));
        var matchingActivity = model.addActivity(new ActivityName("Group walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Training"), ENROL_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0),
                Recurrence.ONCE);
        model.enrolDogs(matchingActivity.getId(), ENROL_DATE, List.of(firstDog.getId(), secondDog.getId()));

        new SearchActivityCommand("group", null, null).execute(model);

        assertEquals(List.of(matchingActivity), model.getFilteredActivityList());
        assertEquals(List.of(model.getDogList().get(0), model.getDogList().get(1)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_noMatchingActivities_clearsDogsAndOwners() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        var activity = model.addActivity(new ActivityName("Walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(dog.getId()));

        new SearchActivityCommand("unknown", null, null).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_activeFilter_refreshesDogsAndOwnersAfterEnrolment() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        var activity = model.addActivity(new ActivityName("Walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);

        new SearchActivityCommand("walk", null, null).execute(model);
        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());

        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(dog.getId()));

        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_activeFilter_removesDogAndOwnerAfterUnenrolment() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        var activity = model.addActivity(new ActivityName("Walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(dog.getId()));

        new SearchActivityCommand("walk", null, null).execute(model);
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());

        model.unenrolDogs(activity.getId(), ENROL_DATE, List.of(dog.getId()));

        assertEquals(List.of(activity), model.getFilteredActivityList());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_dateFilter_showsDogOnlyForEnrolledOccurrence() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        var activity = model.addActivity(new ActivityName("Daily walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.DAILY);
        model.enrolDogs(activity.getId(), ENROL_DATE, List.of(dog.getId()));

        new SearchActivityCommand("daily", ENROL_DATE.plusDays(1), null).execute(model);

        assertEquals(List.of(activity), model.getFilteredActivityList());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());

        model.enrolDogs(activity.getId(), ENROL_DATE.plusDays(1), List.of(dog.getId()));

        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_dogWithMatchingAndUnrelatedEnrolments_usesOnlyMatchingActivity() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        var matchingActivity = model.addActivity(new ActivityName("Group walk"), ENROL_DATE, LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        var unrelatedActivity = model.addActivity(new ActivityName("Training"), ENROL_DATE, LocalTime.of(14, 0),
                LocalTime.of(15, 0), Recurrence.ONCE);
        model.enrolDogs(unrelatedActivity.getId(), ENROL_DATE, List.of(dog.getId()));
        model.enrolDogs(matchingActivity.getId(), ENROL_DATE, List.of(dog.getId()));

        new SearchActivityCommand("group", null, null).execute(model);

        assertEquals(List.of(matchingActivity), model.getFilteredActivityList());
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_recurrenceMatching_isCaseInsensitiveAndIncludedInMessage() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new SearchActivityCommand(null, null, null, Recurrence.DAILY).execute(model);

        assertEquals("Successfully searched activities by recurrence: Daily", result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
    }

    @Test
    public void execute_combinedRecurrenceAndName_filtersOnlyMatchingActivity() throws Exception {
        ModelManager model = modelWithActivities();

        CommandResult result = new SearchActivityCommand("WALK", null, null, Recurrence.DAILY).execute(model);

        assertEquals("Successfully searched activities by name: WALK, recurrence: Daily",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getActivityList().get(1)), model.getFilteredActivityList());
    }

    @Test
    public void execute_recurrenceWithNoMatches_returnsEmptyFilteredList() throws Exception {
        ModelManager model = modelWithActivities();

        new SearchActivityCommand("walk", null, null, Recurrence.WEEKLY).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
    }

    @Test
    public void execute_keepsNewMatchingActivityAfterFilterIsActive() throws Exception {
        ModelManager model = modelWithActivities();
        new SearchActivityCommand("run", null, null).execute(model);
        model.addActivity(new ActivityName("Evening Run"), DATE, LocalTime.of(18, 0),
                LocalTime.of(19, 0), Recurrence.ONCE);

        assertEquals(2, model.getFilteredActivityList().size());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new SearchActivityCommand("run", null, null).execute(null));
    }

    @Test
    public void equals_sameFieldsAndOtherTypes_returnsExpectedResult() {
        SearchActivityCommand command = new SearchActivityCommand("run", DATE, LocalTime.NOON, Recurrence.ONCE);

        assertEquals(command, command);
        assertEquals(command, new SearchActivityCommand("run", DATE, LocalTime.NOON, Recurrence.ONCE));
        assertFalse(command.equals(new SearchActivityCommand("run", DATE, LocalTime.NOON, Recurrence.DAILY)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("command"));
        assertFalse(command.equals(new SearchActivityCommand("other", DATE, LocalTime.NOON, Recurrence.ONCE)));
        assertFalse(command.equals(new SearchActivityCommand("run", DATE.plusDays(1), LocalTime.NOON,
                Recurrence.ONCE)));
        assertFalse(command.equals(new SearchActivityCommand("run", DATE, LocalTime.MIDNIGHT,
                Recurrence.ONCE)));
    }

    private ModelManager modelWithActivities() {
        ModelManager model = new ModelManager();
        model.addActivity(new ActivityName("Dog Run"), DATE, LocalTime.of(16, 0),
                LocalTime.of(17, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Morning Walk"), DATE, LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.DAILY);
        model.addActivity(new ActivityName("Training"), DATE, LocalTime.of(10, 0),
                LocalTime.of(11, 0), Recurrence.WEEKLY);
        return model;
    }
}
