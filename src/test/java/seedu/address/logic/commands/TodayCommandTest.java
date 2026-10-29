package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

public class TodayCommandTest {
    @Test
    public void execute_showsTodayActivitiesDogsAndOwners() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        Activity today = model.addActivity(new ActivityName("Today's walk"), LocalDate.now(), LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        Activity tomorrow = model.addActivity(new ActivityName("Tomorrow's walk"), LocalDate.now().plusDays(1),
                LocalTime.NOON, LocalTime.of(13, 0), Recurrence.ONCE);
        LocalDate previousWeek = LocalDate.now().minusWeeks(1);
        while (previousWeek.getDayOfWeek() != LocalDate.now().getDayOfWeek()) {
            previousWeek = previousWeek.minusDays(1);
        }
        Activity daily = model.addActivity(new ActivityName("Daily walk"), LocalDate.now().minusDays(1),
                LocalTime.of(6, 0), LocalTime.of(7, 0), Recurrence.DAILY);
        Activity weekly = model.addActivity(new ActivityName("Weekly walk"), previousWeek, LocalTime.of(7, 0),
                LocalTime.of(8, 0), Recurrence.WEEKLY);
        Activity empty = model.addActivity(new ActivityName("Empty today"), LocalDate.now(), LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.ONCE);
        model.enrolDogs(today.getId(), LocalDate.now(), List.of(dog.getId()));
        model.enrolDogs(tomorrow.getId(), LocalDate.now().plusDays(1), List.of(dog.getId()));
        model.enrolDogs(daily.getId(), LocalDate.now(), List.of(dog.getId()));
        model.enrolDogs(weekly.getId(), LocalDate.now(), List.of(dog.getId()));

        CommandResult result = new TodayCommand().execute(model);

        assertEquals(TodayCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(today, daily, weekly, empty), model.getFilteredActivityList());
        assertEquals(List.of(model.getDogList().get(0)), model.getFilteredDogList());
        assertEquals(List.of(model.getCustomerList().get(0)), model.getFilteredCustomerList());
    }

    @Test
    public void execute_unrelatedEnrolments_doNotLeakIntoTodayDisplay() throws Exception {
        ModelManager model = new ModelManager();
        model.addCustomer(new Customer(1, "Anna", "91234567", "anna@example.com", "98765432"));
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        model.linkDogs(1, List.of(dog.getId()));
        Activity emptyToday = model.addActivity(new ActivityName("Empty today"), LocalDate.now(), LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);
        Activity tomorrow = model.addActivity(new ActivityName("Tomorrow"), LocalDate.now().plusDays(1),
                LocalTime.NOON, LocalTime.of(13, 0), Recurrence.ONCE);
        model.enrolDogs(tomorrow.getId(), LocalDate.now().plusDays(1), List.of(dog.getId()));

        new TodayCommand().execute(model);

        assertEquals(List.of(emptyToday), model.getFilteredActivityList());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
    }

    @Test
    public void execute_noTodayActivities_showsEmptyRelatedLists() {
        ModelManager model = new ModelManager();
        model.addActivity(new ActivityName("Tomorrow's walk"), LocalDate.now().plusDays(1), LocalTime.NOON,
                LocalTime.of(13, 0), Recurrence.ONCE);

        new TodayCommand().execute(model);

        assertEquals(List.of(), model.getFilteredActivityList());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TodayCommand().execute(null));
    }

    @Test
    public void equals_comparesCommandType() {
        TodayCommand command = new TodayCommand();

        assertEquals(command, command);
        assertEquals(command, new TodayCommand());
        assertFalse(command.equals(null));
        assertFalse(command.equals("today"));
    }
}
