package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class FilterUnlinkedCommandTest {
    @Test
    public void execute_filterUnlinkedDogs_showsOnlyUnownedDogs() throws Exception {
        ModelManager model = modelWithLinkedAndUnlinkedRecords();

        CommandResult result = new FilterUnlinkedCommand(true).execute(model);

        assertEquals("Successfully filtered dogs by unlinked", result.getFeedbackToUser());
        assertEquals(List.of(model.getDogList().get(1)), model.getFilteredDogList());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_filterUnlinkedCustomers_showsOnlyCustomersWithoutDogs() throws Exception {
        ModelManager model = modelWithLinkedAndUnlinkedRecords();

        CommandResult result = new FilterUnlinkedCommand(false).execute(model);

        assertEquals("Successfully filtered customers by unlinked", result.getFeedbackToUser());
        assertEquals(List.of(model.getCustomerList().get(1)), model.getFilteredCustomerList());
        assertTrue(model.getFilteredDogList().isEmpty());
    }

    @Test
    public void equals_sameAndDifferentCategories_returnsExpectedResult() {
        FilterUnlinkedCommand dogCommand = new FilterUnlinkedCommand(true);

        assertTrue(dogCommand.equals(dogCommand));
        assertTrue(dogCommand.equals(new FilterUnlinkedCommand(true)));
        assertTrue(!dogCommand.equals(new FilterUnlinkedCommand(false)));
        assertTrue(!dogCommand.equals(null));
        assertTrue(!dogCommand.equals("command"));
        assertEquals(new FilterUnlinkedCommand(false).hashCode(), new FilterUnlinkedCommand(false).hashCode());
    }

    private ModelManager modelWithLinkedAndUnlinkedRecords() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        model.addCustomer(new Customer(2, "Beth", "92345678", "beth@example.com", "97654321"));
        Dog linked = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.addDog(new Dog(new DogName("Max"), new Breed("Labrador"), new Size("Large")));
        model.linkDogs(1, List.of(linked.getId()));
        return model;
    }
}
