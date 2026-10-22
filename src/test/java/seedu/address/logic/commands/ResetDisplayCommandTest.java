package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests that the view reset shows all stored records. */
public class ResetDisplayCommandTest {
    @Test
    public void execute_restoresRecordsAndKeepsAllActivitiesVisible() {
        ModelManager model = new ModelManager();
        Customer customer = new Customer(1, "Alex Tan", "91234567", "alex@example.com", "98765432");
        model.addCustomer(customer);
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Activity past = addActivity(model, LocalDate.of(2025, 1, 1), Recurrence.ONCE);
        Activity future = addActivity(model, LocalDate.of(2028, 1, 1), Recurrence.WEEKLY);
        model.updateFilteredCustomerList(unused -> false);
        model.updateFilteredDogList(unused -> false);

        CommandResult result = new ResetDisplayCommand().execute(model);

        assertEquals(ResetDisplayCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(customer), model.getFilteredCustomerList());
        assertEquals(List.of(dog), model.getFilteredDogList());
        assertEquals(List.of(past, future), model.getActivityList());
    }

    @Test
    public void execute_emptyModel_showsEmptyLists() {
        ModelManager model = new ModelManager();

        new ResetDisplayCommand().execute(model);

        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getActivityList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ResetDisplayCommand().execute(null));
    }

    private static Activity addActivity(ModelManager model, LocalDate date, Recurrence recurrence) {
        return model.addActivity(new ActivityName("Walk"), date, LocalTime.of(9, 0),
                LocalTime.of(10, 0), recurrence);
    }
}
