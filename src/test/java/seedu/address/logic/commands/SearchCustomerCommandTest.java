package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class SearchCustomerCommandTest {
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
