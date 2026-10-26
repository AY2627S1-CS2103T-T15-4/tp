package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
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

/** Tests that the view reset shows all records and activities that can still occur. */
public class ResetDisplayCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-22T04:00:00Z"), ZoneOffset.UTC);

    @Test
    public void execute_restoresRecordsAndShowsTodayAndFutureOccurrences() {
        ModelManager model = new ModelManager();
        Customer customer = new Customer(1, "Alex Tan", "91234567", "alex@example.com", "98765432");
        model.addCustomer(customer);
        Dog dog = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        Activity pastOnce = addActivity(model, TODAY.minusDays(1), Recurrence.ONCE);
        Activity todayOnce = addActivity(model, TODAY, Recurrence.ONCE);
        Activity futureOnce = addActivity(model, TODAY.plusDays(1), Recurrence.ONCE);
        Activity pastDaily = addActivity(model, TODAY.minusDays(2), Recurrence.DAILY);
        Activity pastWeekly = addActivity(model, TODAY.minusWeeks(1), Recurrence.WEEKLY);
        model.updateFilteredCustomerList(unused -> false);
        model.updateFilteredDogList(unused -> false);

        CommandResult result = new ResetDisplayCommand(CLOCK).execute(model);

        assertEquals(String.format(ResetDisplayCommand.MESSAGE_SUCCESS, TODAY), result.getFeedbackToUser());
        assertEquals(List.of(customer), model.getFilteredCustomerList());
        assertEquals(List.of(dog), model.getFilteredDogList());
        assertEquals(List.of(todayOnce, futureOnce, pastDaily, pastWeekly), model.getFilteredActivityList());
        assertEquals(List.of(pastOnce, todayOnce, futureOnce, pastDaily, pastWeekly), model.getActivityList());
    }

    @Test
    public void execute_emptyModel_showsEmptyLists() {
        ModelManager model = new ModelManager();

        new ResetDisplayCommand(CLOCK).execute(model);

        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredActivityList());
    }

    @Test
    public void execute_afterActivityFilter_keepsUpcomingViewAfterNewActivity() {
        ModelManager model = new ModelManager();
        Activity upcoming = addActivity(model, TODAY.plusDays(1), Recurrence.ONCE);
        new SearchActivityCommand("not present", null, null).execute(model);

        new ResetDisplayCommand(CLOCK).execute(model);
        addActivity(model, TODAY.minusDays(1), Recurrence.ONCE);

        assertEquals(List.of(upcoming), model.getFilteredActivityList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ResetDisplayCommand(CLOCK).execute(null));
    }

    @Test
    public void equals_sameCommandType() {
        ResetDisplayCommand command = new ResetDisplayCommand(CLOCK);

        assertEquals(command, new ResetDisplayCommand(CLOCK));
        assertNotEquals(command, new Object());
    }

    private static Activity addActivity(ModelManager model, LocalDate date, Recurrence recurrence) {
        return model.addActivity(new ActivityName("Walk"), date, LocalTime.of(9, 0),
                LocalTime.of(10, 0), recurrence);
    }
}
