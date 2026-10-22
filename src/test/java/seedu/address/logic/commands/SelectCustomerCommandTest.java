package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Exercises selecting a customer and the related dogs and activities. */
public class SelectCustomerCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager(new AddressBook(), new UserPrefs());

    @Test
    public void execute_linkedDogs_filtersDogsAndCurrentOrFutureActivities() throws Exception {
        Customer owner = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
        Customer other = new Customer(2, "Amy Tan", "81234567", "amy@gmail.com", "87654321");
        model.addCustomer(owner);
        model.addCustomer(other);
        Dog cookie = model.addDog(dog("Cookie"));
        Dog max = model.addDog(dog("Max"));
        Dog otherDog = model.addDog(dog("Buddy"));
        model.linkDogs(owner.getId(), List.of(cookie.getId(), max.getId()));
        model.linkDogs(other.getId(), List.of(otherDog.getId()));

        LocalDate today = LocalDate.now();
        Activity past = activity("Past walk", today.minusDays(1), Recurrence.ONCE);
        Activity current = activity("Today walk", today, Recurrence.ONCE);
        Activity future = activity("Future walk", today.plusDays(1), Recurrence.ONCE);
        Activity otherActivity = activity("Buddy walk", today.plusDays(2), Recurrence.ONCE);
        model.enrolDogs(past.getId(), List.of(cookie.getId()));
        model.enrolDogs(current.getId(), List.of(cookie.getId()));
        model.enrolDogs(future.getId(), List.of(max.getId()));
        model.enrolDogs(otherActivity.getId(), List.of(otherDog.getId()));
        model.markActivity(current.getId());

        assertEquals("Selected customer 1: Lily Koh", execute("select customer 1"));
        assertEquals(List.of(owner), model.getFilteredCustomerList());
        assertEquals(List.of(cookie.getId(), max.getId()),
                model.getFilteredDogList().stream().map(Dog::getId).toList());
        assertEquals(List.of(future.getId()),
                model.getFilteredActivityList().stream().map(Activity::getId).toList());
    }

    @Test
    public void execute_customerWithoutDogs_showsCustomerOnly() throws Exception {
        model.addCustomer(new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432"));
        Dog buddy = model.addDog(dog("Buddy"));
        Activity walk = activity("Future walk", LocalDate.now().plusDays(1), Recurrence.ONCE);
        model.enrolDogs(walk.getId(), List.of(buddy.getId()));

        assertEquals("Selected customer 1: Lily Koh", execute("select   customer   1"));
        assertEquals(1, model.getFilteredCustomerList().size());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredActivityList());
    }

    @Test
    public void execute_laterLinkAndEnrol_updateTheSelectedView() throws Exception {
        Customer owner = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
        model.addCustomer(owner);
        Dog cookie = model.addDog(dog("Cookie"));
        execute("select customer 1");

        model.linkDogs(owner.getId(), List.of(cookie.getId()));
        assertEquals(List.of(cookie.getId()), model.getFilteredDogList().stream().map(Dog::getId).toList());

        Activity walk = activity("Future walk", LocalDate.now().plusDays(1), Recurrence.ONCE);
        model.enrolDogs(walk.getId(), List.of(cookie.getId()));
        assertEquals(List.of(walk.getId()), model.getFilteredActivityList().stream().map(Activity::getId).toList());

        model.unlinkDogs(List.of(cookie.getId()));
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredActivityList());
        assertEquals(List.of(owner), model.getFilteredCustomerList());
    }

    @Test
    public void execute_missingId_leavesFiltersUnchanged() throws Exception {
        Customer owner = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
        model.addCustomer(owner);
        execute("select customer 1");
        List<Customer> customers = List.copyOf(model.getFilteredCustomerList());

        CommandException exception = assertThrows(CommandException.class, () -> execute("select customer 99"));
        assertEquals(SelectCustomerCommand.MESSAGE_INVALID_ID, exception.getMessage());
        assertEquals(customers, model.getFilteredCustomerList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SelectCustomerCommand(1).execute(null));
    }

    @Test
    public void equals_sameIdOnly() {
        SelectCustomerCommand command = new SelectCustomerCommand(21);
        assertEquals(command, command);
        assertEquals(command, new SelectCustomerCommand(21));
        assertNotEquals(command, new SelectCustomerCommand(22));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
        assertEquals(command.toString(), new SelectCustomerCommand(21).toString());
    }

    private String execute(String input) throws Exception {
        return parser.parseCommand(input).execute(model).getFeedbackToUser();
    }

    private Activity activity(String name, LocalDate date, Recurrence recurrence) {
        return model.addActivity(new ActivityName(name), date, LocalTime.of(9, 0), LocalTime.of(10, 0), recurrence);
    }

    private Dog dog(String name) {
        return new Dog(new DogName(name), new Breed("Poodle"), new Size("small"));
    }
}
