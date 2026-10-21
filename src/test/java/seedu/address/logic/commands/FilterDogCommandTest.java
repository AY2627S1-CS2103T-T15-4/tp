package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class FilterDogCommandTest {
    @Test
    public void execute_combinedFields_filtersDogsAndLinkedCustomers() throws Exception {
        ModelManager model = modelWithDogs();

        CommandResult result = new FilterDogCommand("waffle", "golden retriever", "medium", 1).execute(model);

        assertEquals("Successfully filtered dogs by name: waffle, breed: golden retriever, size: medium, id: 1",
                result.getFeedbackToUser());
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_noMatchingDogs_clearsBothFilteredLists() throws Exception {
        ModelManager model = modelWithDogs();

        new FilterDogCommand("missing", null, null, null).execute(model);

        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_unownedDog_doesNotDisplayCustomers() throws Exception {
        ModelManager model = modelWithDogs();

        new FilterDogCommand(null, null, null, 3).execute(model);

        assertEquals(1, model.getFilteredDogList().size());
        assertTrue(model.getFilteredCustomerList().isEmpty());
    }

    @Test
    public void execute_unknownId_throwsCommandException() {
        ModelManager model = new ModelManager();

        assertThrows(CommandException.class, () -> new FilterDogCommand(null, null, null, 99).execute(model));
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
