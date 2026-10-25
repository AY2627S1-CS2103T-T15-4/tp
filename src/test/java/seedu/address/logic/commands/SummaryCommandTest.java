package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests summary counts, date consistency and preservation of model state. */
public class SummaryCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-24T23:59:59Z"), ZoneOffset.UTC);

    @Test
    public void execute_emptyData_showsSixZeroCountsAndDate() {
        CommandResult result = new SummaryCommand(CLOCK).execute(new ModelManager());

        assertEquals("Overall: 0 dogs | 0 customers\n"
                + "Activity schedules: 0 total | 0 from today onwards\n"
                + "Today (2026-10-24): 0 activities | 0 distinct dogs enrolled", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_mixedData_countsAllRecordsAndDistinctDogsDespiteFilters() throws Exception {
        Model model = mixedModel();
        model.updateFilteredDogList(unused -> false);
        model.updateFilteredCustomerList(unused -> false);
        model.updateFilteredActivityList(unused -> false);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals("Overall: 3 dogs | 2 customers\n"
                + "Activity schedules: 7 total | 6 from today onwards\n"
                + "Today (2026-10-24): 3 activities | 2 distinct dogs enrolled",
                new SummaryCommand(CLOCK).execute(model).getFeedbackToUser());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredActivityList());
        // The predicates must remain active when later records are added.
        model.addDog(dog(0));
        addActivity(model, TODAY, Recurrence.ONCE);
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredActivityList());
    }

    @Test
    public void execute_selectedDogOrCustomer_preservesSelectionAfterMutation() throws Exception {
        for (boolean selectDog : new boolean[] {true, false}) {
            Model model = mixedModel();
            Model expected = mixedModel();
            if (selectDog) {
                model.selectDog(9);
                expected.selectDog(9);
            } else {
                model.selectCustomer(1);
                expected.selectCustomer(1);
            }
            new SummaryCommand(CLOCK).execute(model);
            assertEquals(expected, model);
            model.unlinkDogs(List.of(9));
            expected.unlinkDogs(List.of(9));
            assertEquals(expected, model);
        }
    }

    @Test
    public void execute_recurrenceAndFirstDateBoundaries_includesEmptyActivities() {
        for (Recurrence recurrence : Recurrence.values()) {
            for (int offset : new int[] {-7, -1, 0, 1}) {
                Model model = new ModelManager();
                addActivity(model, TODAY.plusDays(offset), recurrence);
                int upcoming = recurrence == Recurrence.ONCE && offset < 0 ? 0 : 1;
                int today = offset == 0 || offset < 0 && (recurrence == Recurrence.DAILY
                        || recurrence == Recurrence.WEEKLY && offset == -7) ? 1 : 0;
                assertEquals("Overall: 0 dogs | 0 customers\nActivity schedules: 1 total | " + upcoming
                        + " from today onwards\nToday (2026-10-24): " + today
                        + " activities | 0 distinct dogs enrolled",
                        new SummaryCommand(CLOCK).execute(model).getFeedbackToUser());
            }
        }
    }

    @Test
    public void execute_reusedCommand_readsDateAtExecutionOnceAndReflectsMutations() {
        AdvancingClock clock = new AdvancingClock(Instant.parse("2026-10-24T15:59:59Z"), ZoneOffset.ofHours(8));
        SummaryCommand command = new SummaryCommand(clock);
        Model model = new ModelManager();
        addActivity(model, TODAY, Recurrence.ONCE);
        // Construction precedes midnight; execution happens after midnight in the clock's local zone.
        clock.instant = Instant.parse("2026-10-24T16:00:00Z");
        assertEquals("Overall: 0 dogs | 0 customers\n"
                + "Activity schedules: 1 total | 0 from today onwards\n"
                + "Today (2026-10-25): 0 activities | 0 distinct dogs enrolled",
                command.execute(model).getFeedbackToUser());
        assertEquals(1, clock.reads);
        model.addDog(dog(0));
        addActivity(model, TODAY.plusDays(2), Recurrence.DAILY);
        clock.instant = Instant.parse("2026-10-25T16:00:00Z");
        assertEquals("Overall: 1 dogs | 0 customers\n"
                + "Activity schedules: 2 total | 1 from today onwards\n"
                + "Today (2026-10-26): 1 activities | 0 distinct dogs enrolled",
                command.execute(model).getFeedbackToUser());
        assertEquals(2, clock.reads);
    }

    @Test
    public void execute_dateChangesDuringExecution_usesSameDateForEnrolmentQuery() {
        AdvancingClock clock = new AdvancingClock(CLOCK.instant(), ZoneOffset.UTC);
        Model model = new ModelManager() {
            @Override
            public List<Dog> getDogsEnrolledOn(LocalDate date) {
                assertEquals(TODAY, date);
                return List.of(dog(1));
            }
        };
        addActivity(model, TODAY, Recurrence.ONCE);
        assertEquals("Overall: 0 dogs | 0 customers\n"
                + "Activity schedules: 1 total | 1 from today onwards\n"
                + "Today (2026-10-24): 1 activities | 1 distinct dogs enrolled",
                new SummaryCommand(clock).execute(model).getFeedbackToUser());
        assertEquals(1, clock.reads);
    }

    @Test
    public void nullInputsAndEquality() {
        assertThrows(NullPointerException.class, () -> new SummaryCommand(null));
        assertThrows(NullPointerException.class, () -> new SummaryCommand(CLOCK).execute(null));
        SummaryCommand command = new SummaryCommand(CLOCK);
        assertEquals(command, command);
        assertEquals(command, new SummaryCommand(CLOCK));
        assertNotEquals(command, new Object());
        assertNotEquals(command, null);
    }

    private static Model mixedModel() throws Exception {
        AddressBook book = new AddressBook();
        book.addDogFromStorage(dog(9));
        book.addDogFromStorage(dog(4));
        book.addDogFromStorage(dog(7));
        book.addCustomer(new Customer(1, "Alex", "91234567", "alex@example.com", "98765432"));
        book.addCustomer(new Customer(2, "Alex", "81234567", "other@example.com", "87654321"));
        book.linkDogs(1, List.of(9));
        book.addActivity(new ActivityName("Past"), TODAY.minusDays(1), LocalTime.of(7, 0),
                LocalTime.of(8, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Today"), TODAY, LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Future"), TODAY.plusDays(1), LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Daily"), TODAY.minusDays(10), LocalTime.of(9, 0),
                LocalTime.of(10, 0), Recurrence.DAILY);
        book.addActivity(new ActivityName("Weekly today"), TODAY.minusWeeks(1), LocalTime.of(10, 0),
                LocalTime.of(11, 0), Recurrence.WEEKLY);
        book.addActivity(new ActivityName("Weekly later"), TODAY.minusDays(1), LocalTime.of(11, 0),
                LocalTime.of(12, 0), Recurrence.WEEKLY);
        book.addActivity(new ActivityName("Future daily"), TODAY.plusDays(1), LocalTime.of(12, 0),
                LocalTime.of(13, 0), Recurrence.DAILY);
        book.addEnrolmentFromStorage(new Enrolment(2, 9, TODAY));
        book.addEnrolmentFromStorage(new Enrolment(4, 9, TODAY));
        book.addEnrolmentFromStorage(new Enrolment(4, 4, TODAY));
        book.addEnrolmentFromStorage(new Enrolment(1, 7, TODAY.minusDays(1)));
        book.addEnrolmentFromStorage(new Enrolment(3, 7, TODAY.plusDays(1)));
        return new ModelManager(book, new UserPrefs());
    }

    private static Dog dog(int id) {
        return new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
    }

    private static void addActivity(Model model, LocalDate date, Recurrence recurrence) {
        model.addActivity(new ActivityName("Walk"), date, LocalTime.of(8, 0), LocalTime.of(9, 0), recurrence);
    }

    /** Advances one day after every read to detect inconsistent date reads within one execution. */
    private static class AdvancingClock extends Clock {
        private final ZoneId zone;
        private Instant instant;
        private int reads;

        AdvancingClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new AdvancingClock(instant, newZone);
        }

        @Override
        public Instant instant() {
            Instant result = instant;
            instant = instant.plusSeconds(86400);
            reads++;
            return result;
        }
    }
}
