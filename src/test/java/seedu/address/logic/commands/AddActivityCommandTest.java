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
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class AddActivityCommandTest {
    private static final ActivityName NAME = new ActivityName("Training");
    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(11, 0);

    @Test
    public void execute_validSchedule_addsWithIdAndPreservesRecordsAndFilters() throws Exception {
        ModelManager model = new ModelManager();
        model.addDog(new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("small")));
        model.addCustomer(new Customer(new Name("Alice Tan"), new Phone("91234567"),
                new Email("alice@example.com"), new BackupContact("98765432")));
        List<?> dogs = List.copyOf(model.getDogList());
        List<?> customers = List.copyOf(model.getCustomerList());
        model.updateFilteredDogList(unused -> false);
        model.updateFilteredCustomerList(unused -> false);
        AddActivityCommand command = command(Recurrence.WEEKLY);
        CommandResult result = command.execute(model);
        Activity activity = model.getActivityList().get(0);
        assertEquals(new Activity(1, NAME, DATE, START, END, Recurrence.WEEKLY), activity);
        assertEquals("New activity added: ID 1; Training; First date: 2026-10-20; 10:00-11:00; WEEKLY",
                result.getFeedbackToUser());
        assertEquals(dogs, model.getDogList());
        assertEquals(customers, model.getCustomerList());
        assertTrue(model.getFilteredDogList().isEmpty());
        assertTrue(model.getFilteredCustomerList().isEmpty());
        command.execute(model);
        assertEquals(2, model.getActivityList().get(1).getId());
    }

    @Test
    public void execute_exhaustedIds_throwsWithoutMutation() {
        WatchDog book = new WatchDog();
        book.setNextActivityId((long) Integer.MAX_VALUE + 1);
        ModelManager model = new ModelManager(book, new UserPrefs());
        assertThrows(CommandException.class, "No activity IDs remain available.", () ->
                command(Recurrence.ONCE).execute(model));
        assertEquals(book, model.getWatchDog());
        assertThrows(NullPointerException.class, () -> command(Recurrence.ONCE).execute(null));
    }

    @Test
    public void equals_detailsAndOtherTypes_returnsExpectedResult() {
        AddActivityCommand command = command(Recurrence.ONCE);
        assertEquals(command, command);
        assertEquals(command, command(Recurrence.ONCE));
        assertEquals(command.hashCode(), command(Recurrence.ONCE).hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("command"));
        assertFalse(command.equals(command(Recurrence.DAILY)));
        assertTrue(command.toString().contains("Training"));
        assertThrows(IllegalArgumentException.class, () ->
                new AddActivityCommand(NAME, DATE, END, START, Recurrence.ONCE));
    }

    private AddActivityCommand command(Recurrence recurrence) {
        return new AddActivityCommand(NAME, DATE, START, END, recurrence);
    }
}
