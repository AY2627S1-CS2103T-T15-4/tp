package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import seedu.address.model.dog.Size;

public class SearchCustomerCommandTest {
    @Test
    public void execute_callerChangesDogIds_preservesOriginalQueryAndLiveOwnership() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        Set<Integer> ids = new HashSet<>(Set.of(1));
        SearchCustomerCommand command = new SearchCustomerCommand(null, null, null, null, ids);
        ids.clear();
        ids.add(3);

        assertEquals("Successfully searched customers by dog ids: 1", command.execute(model).getFeedbackToUser());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
        model.unlinkDogs(List.of(1));
        assertTrue(model.getFilteredCustomerList().isEmpty());
        model.linkDogs(2, List.of(1));
        assertEquals(List.of(model.getCustomerList().get(1)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_combinedFields_matchesAllRequestedFields() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        CommandResult result = new SearchCustomerCommand(
                "anna", "91234567", "anna@example.com", "98765432", Set.of(1, 2)).execute(model);

        assertEquals("Successfully searched customers by name: anna, phone: 91234567, "
                + "email: anna@example.com, dog ids: 1 2, backup contact: 98765432",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
        assertEquals(List.of(model.getDogList().get(0), model.getDogList().get(1)),
                model.getFilteredDogList());
    }

    @Test
    public void execute_nameMatching_isCaseInsensitiveSubstring() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        new SearchCustomerCommand("AN", null, null, null, null).execute(model);

        assertEquals(2, model.getFilteredCustomerList().size());
        assertEquals(3, model.getFilteredDogList().size());
    }

    @Test
    public void execute_requiresAllRequestedDogs() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        new SearchCustomerCommand(null, null, null, null, Set.of(1, 3)).execute(model);

        assertTrue(model.getFilteredCustomerList().isEmpty());
        assertTrue(model.getFilteredDogList().isEmpty());
    }

    @Test
    public void execute_invalidIds_preservesPreviousDisplay() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        Activity activity = model.addActivity(new ActivityName("Walk"), LocalDate.now().plusDays(7),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), activity.getStartDate(), List.of(1));
        new SearchCustomerCommand(null, null, null, null, null, 1).execute(model);
        var customers = List.copyOf(model.getFilteredCustomerList());
        var dogs = List.copyOf(model.getFilteredDogList());
        String summary = model.getActivityEnrolmentSummary(activity);

        for (int id : new int[]{-1, 0}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new SearchCustomerCommand(null, null, null, null, Set.of(1, id)));
        }
        for (int id : new int[]{999, Integer.MAX_VALUE}) {
            assertThrows(CommandException.class, "Error in searching customers:\nPlease enter a valid dog ID", () ->
                    new SearchCustomerCommand(null, null, null, null, Set.of(1, id)).execute(model));
            assertThrows(CommandException.class, SearchCustomerCommand.MESSAGE_INVALID_CUSTOMER_ID, () ->
                    new SearchCustomerCommand(null, null, null, null, null, id).execute(model));
            assertEquals(customers, model.getFilteredCustomerList());
            assertEquals(dogs, model.getFilteredDogList());
            assertEquals(List.of(activity), model.getFilteredActivityList());
            assertEquals(summary, model.getActivityEnrolmentSummary(activity));
        }
    }

    @Test
    public void execute_unknownDogId_throwsCommandException() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        assertThrows(CommandException.class, SearchCustomerCommand.MESSAGE_INVALID_DOG_ID, () ->
                new SearchCustomerCommand(null, null, null, null, Set.of(100)).execute(model));
    }

    @Test
    public void execute_customerId_filtersCustomerAndLinkedDogs() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        CommandResult result = new SearchCustomerCommand(null, null, null, null, null, 2).execute(model);

        assertEquals("Successfully searched customers by customer id: 2", result.getFeedbackToUser());
        assertEquals(List.of(model.getCustomerList().get(1)), model.getFilteredCustomerList());
        assertEquals(List.of(model.getDogList().get(2)), model.getFilteredDogList());
    }

    @Test
    public void execute_unknownCustomerId_throwsCommandException() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        assertThrows(CommandException.class, SearchCustomerCommand.MESSAGE_INVALID_CUSTOMER_ID, () ->
                new SearchCustomerCommand(null, null, null, null, null, 99).execute(model));
    }

    @Test
    public void execute_eachFieldMismatch_excludesCustomer() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();

        new SearchCustomerCommand("missing", null, null, null, null).execute(model);
        assertTrue(model.getFilteredCustomerList().isEmpty());
        new SearchCustomerCommand(null, "99999999", null, null, null).execute(model);
        assertTrue(model.getFilteredCustomerList().isEmpty());
        new SearchCustomerCommand(null, null, "other@example.com", null, null).execute(model);
        assertTrue(model.getFilteredCustomerList().isEmpty());
        new SearchCustomerCommand(null, null, null, "99999999", null).execute(model);
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_refreshesLinkedDogsAfterCustomerEdit() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        new SearchCustomerCommand("anna", null, null, null, null).execute(model);
        Customer anna = model.getCustomerList().get(0);
        model.setCustomer(anna, new Customer(1, "Beth Lee", "91234567",
                "anna@example.com", "98765432"));

        assertEquals(1, model.getFilteredCustomerList().size());
        assertEquals(1, model.getFilteredDogList().size());
    }

    @Test
    public void execute_refreshesLinkedDogsAfterRelationshipChange() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        new SearchCustomerCommand("anna", null, null, null, null).execute(model);
        model.unlinkDogs(List.of(1, 2));

        assertEquals(2, model.getFilteredCustomerList().size());
        assertEquals(1, model.getFilteredDogList().size());
    }

    @Test
    public void execute_customerThenDogSearchAndEdit_keepsMatchingActivityVisible() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        Activity activity = model.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), activity.getStartDate(), List.of(1));

        new SearchCustomerCommand("anna", null, null, null, null).execute(model);
        new SearchDogCommand(null, null, null, 1).execute(model);
        EditDogCommand.EditDogDescriptor descriptor = new EditDogCommand.EditDogDescriptor();
        descriptor.setName(new DogName("Buddy Updated"));
        new EditDogCommand(1, descriptor).execute(model);

        assertEquals(List.of(activity), model.getFilteredActivityList());
        assertTrue(model.getActivityEnrolmentSummary(activity).contains("Buddy Updated"));
    }

    @Test
    public void execute_customerSearch_ignoresUnownedEnrolments() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        Dog unowned = model.addDog(new Dog(new DogName("Unowned"), new Breed("Poodle"), new Size("small")));
        Activity activity = model.addActivity(new ActivityName("Unowned walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.enrolDogs(activity.getId(), activity.getStartDate(), List.of(unowned.getId()));

        new SearchCustomerCommand("anna", null, null, null, null).execute(model);

        assertTrue(model.getFilteredActivityList().isEmpty());
        assertEquals("", model.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void execute_customerSearch_filtersActivitiesAndShowsMatchingDogDates() throws Exception {
        ModelManager model = modelWithCustomersAndDogs();
        Activity activity = model.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        model.enrolDogs(activity.getId(), LocalDate.of(2028, 3, 7), List.of(1));

        new SearchCustomerCommand("anna", null, null, null, null).execute(model);

        assertEquals(List.of(activity), model.getFilteredActivityList());
        assertEquals("Enrolments:\n1. Buddy: 2028-03-07", model.getActivityEnrolmentSummary(activity));
    }

    @Test
    public void equals_differentFields_returnsFalse() {
        SearchCustomerCommand command = new SearchCustomerCommand(
                "anna", "91234567", "anna@example.com", "98765432", Set.of(1), 1);

        assertEquals(command, command);
        assertTrue(!command.equals(null));
        assertTrue(!command.equals("command"));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "other", "91234567", "anna@example.com", "98765432", Set.of(1))));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "anna", "99999999", "anna@example.com", "98765432", Set.of(1))));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "anna", "91234567", "other@example.com", "98765432", Set.of(1))));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "anna", "91234567", "anna@example.com", "99999999", Set.of(1))));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "anna", "91234567", "anna@example.com", "98765432", Set.of(2))));
        assertTrue(!command.equals(new SearchCustomerCommand(
                "anna", "91234567", "anna@example.com", "98765432", Set.of(1), 2)));
    }

    private ModelManager modelWithCustomersAndDogs() throws Exception {
        ModelManager model = new ModelManager();
        Customer anna = new Customer(1, "Anna Lee", "91234567", "anna@example.com", "98765432");
        model.addCustomer(anna);
        model.addCustomer(new Customer(2, "Annabelle Keel", "92345678", "annabelle@example.com", "97654321"));
        Dog buddy = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Dog max = model.addDog(new Dog(new DogName("Max"), new Breed("Labrador"), new Size("Large")));
        Dog kia = model.addDog(new Dog(new DogName("Kia"), new Breed("Pointer"), new Size("Medium")));
        model.linkDogs(1, List.of(buddy.getId(), max.getId()));
        model.linkDogs(2, List.of(kia.getId()));
        return model;
    }
}
