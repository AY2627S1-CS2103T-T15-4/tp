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

/** Exercises selecting a dog and the related owner and activity columns. */
public class SelectDogCommandTest {
    private final AddressBookParser parser = new AddressBookParser();
    private final Model model = new ModelManager(new AddressBook(), new UserPrefs());

    @Test
    public void execute_linkedDog_filtersOwnerAndCurrentOrFutureActivities() throws Exception {
        Customer owner = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
        model.addCustomer(owner);
        model.addCustomer(new Customer(2, "Amy Tan", "81234567", "amy@gmail.com", "87654321"));
        Dog buddy = model.addDog(dog("Buddy"));
        Dog max = model.addDog(dog("Max"));
        model.linkDogs(owner.getId(), List.of(buddy.getId()));

        LocalDate today = LocalDate.now();
        Activity past = activity("Past walk", today.minusDays(1), Recurrence.ONCE);
        Activity current = activity("Today walk", today, Recurrence.ONCE);
        Activity future = activity("Future walk", today.plusDays(1), Recurrence.ONCE);
        Activity daily = activity("Daily walk", today.minusDays(3), Recurrence.DAILY);
        Activity weekly = activity("Weekly walk", today.minusWeeks(1), Recurrence.WEEKLY);
        Activity otherDog = activity("Max walk", today.plusDays(2), Recurrence.ONCE);
        model.enrolDogs(past.getId(), List.of(buddy.getId()));
        model.enrolDogs(current.getId(), List.of(buddy.getId()));
        model.enrolDogs(future.getId(), List.of(buddy.getId()));
        model.enrolDogs(daily.getId(), List.of(buddy.getId()));
        model.enrolDogs(weekly.getId(), List.of(buddy.getId()));
        model.enrolDogs(otherDog.getId(), List.of(max.getId()));
        model.markActivity(current.getId());
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals("Selected dog " + buddy.getId() + ": Buddy", execute("select dog " + buddy.getId()));
        assertEquals(List.of(buddy.getId()), model.getFilteredDogList().stream().map(Dog::getId).toList());
        assertEquals(List.of(owner), model.getFilteredCustomerList());
        assertEquals(List.of(future.getId(), daily.getId(), weekly.getId()),
                model.getFilteredActivityList().stream().map(Activity::getId).toList());
        assertEquals(before, model.getAddressBook());
        model.markActivity(future.getId());
        assertEquals(List.of(daily.getId(), weekly.getId()),
                model.getFilteredActivityList().stream().map(Activity::getId).toList());
    }

    @Test
    public void execute_unlinkedDog_clearsOwnerAndActivities() throws Exception {
        model.addCustomer(new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432"));
        Dog buddy = model.addDog(dog("Buddy"));
        activity("Future walk", LocalDate.now().plusDays(1), Recurrence.ONCE);

        assertEquals("Selected dog " + buddy.getId() + ": Buddy", execute("select   dog   " + buddy.getId()));
        assertEquals(List.of(buddy.getId()), model.getFilteredDogList().stream().map(Dog::getId).toList());
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredActivityList());
    }

    @Test
    public void execute_laterChanges_updateTheSelectedView() throws Exception {
        Customer owner = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");
        model.addCustomer(owner);
        Dog buddy = model.addDog(dog("Buddy"));
        execute("select dog " + buddy.getId());

        model.linkDogs(owner.getId(), List.of(buddy.getId()));
        assertEquals(List.of(owner.getId()), model.getFilteredCustomerList().stream().map(Customer::getId).toList());

        Activity walk = activity("Future walk", LocalDate.now().plusDays(1), Recurrence.ONCE);
        model.enrolDogs(walk.getId(), List.of(buddy.getId()));
        assertEquals(List.of(walk.getId()), model.getFilteredActivityList().stream().map(Activity::getId).toList());

        Dog stored = model.getDogList().stream().filter(dog -> dog.getId() == buddy.getId()).findFirst().orElseThrow();
        model.setDog(stored, new Dog(stored.getId(), new DogName("Biscuit"), stored.getBreed(), stored.getSize(),
                stored.getOwnerId().orElse(null)));
        assertEquals("Biscuit", model.getFilteredDogList().get(0).getName().fullName);

        Customer editedOwner = new Customer(owner.getId(), "Lily Koh", "80000000", "lily@gmail.com", "98765432");
        model.setCustomer(owner, editedOwner);
        assertEquals("80000000", model.getFilteredCustomerList().get(0).getPhone().value);

        model.unenrolDogs(walk.getId(), List.of(buddy.getId()));
        assertEquals(List.of(), model.getFilteredActivityList());
        model.unlinkDogs(List.of(buddy.getId()));
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(buddy.getId()), model.getFilteredDogList().stream().map(Dog::getId).toList());
    }

    @Test
    public void execute_missingId_leavesFiltersUnchanged() throws Exception {
        Dog buddy = model.addDog(dog("Buddy"));
        execute("select dog " + buddy.getId());
        List<Dog> dogs = List.copyOf(model.getFilteredDogList());
        List<Customer> customers = List.copyOf(model.getFilteredCustomerList());
        List<Activity> activities = List.copyOf(model.getFilteredActivityList());

        CommandException exception = assertThrows(CommandException.class, () -> execute("select dog 99"));
        assertEquals(SelectDogCommand.MESSAGE_INVALID_ID, exception.getMessage());
        assertEquals(dogs, model.getFilteredDogList());
        assertEquals(customers, model.getFilteredCustomerList());
        assertEquals(activities, model.getFilteredActivityList());
    }

    @Test
    public void equals_sameIdOnly() {
        SelectDogCommand command = new SelectDogCommand(21);
        assertEquals(command, command);
        assertEquals(command, new SelectDogCommand(21));
        assertNotEquals(command, new SelectDogCommand(22));
        assertNotEquals(command, null);
        assertNotEquals(command, new Object());
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
