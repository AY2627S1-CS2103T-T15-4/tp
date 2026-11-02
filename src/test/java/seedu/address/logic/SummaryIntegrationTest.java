package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.SummaryCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.JsonWatchDogStorage;
import seedu.address.storage.StorageManager;

/** Checks populated summaries across persistence, filters, and command history. */
public class SummaryIntegrationTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 24);
    private static final Clock CLOCK = Clock.fixed(DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    private static final List<Enrolment> BOOKINGS = List.of(
            new Enrolment(2, 1, DATE), new Enrolment(4, 1, DATE), new Enrolment(4, 2, DATE.plusDays(1)));

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_reloadedSummaryAndHistory_preservesCountsRecordsAndFilters() throws Exception {
        JsonWatchDogStorage dataStorage = new JsonWatchDogStorage(temporaryFolder.resolve("watchdog.json"));
        WatchDog original = populatedBook();
        dataStorage.saveWatchDog(original);
        ModelManager model = new ModelManager(dataStorage.readWatchDog().orElseThrow(), new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(dataStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        assertEquals(original, model.getWatchDog());
        assertSummary(logic, model, dataStorage, 2, 1);

        model.updateFilteredDogList(unused -> false);
        model.updateFilteredCustomerList(unused -> false);
        model.updateFilteredActivityList(unused -> false);
        assertSummary(logic, model, dataStorage, 2, 1);

        logic.execute("deletedog 1");
        assertSummary(logic, model, dataStorage, 1, 0);
        assertEquals(List.of(BOOKINGS.get(2)), model.getEnrolmentList());
        WatchDog deleted = new WatchDog(model.getWatchDog());

        logic.execute("undo");
        assertSummary(logic, model, dataStorage, 2, 1);
        assertEquals(BOOKINGS, model.getEnrolmentList());
        assertEquals(original, model.getWatchDog());

        logic.execute("redo");
        assertSummary(logic, model, dataStorage, 1, 0);
        assertEquals(List.of(BOOKINGS.get(2)), model.getEnrolmentList());
        assertEquals(deleted, model.getWatchDog());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(List.of(), model.getFilteredCustomerList());
        assertEquals(List.of(), model.getFilteredActivityList());
    }

    private static void assertSummary(LogicManager logic, ModelManager model, JsonWatchDogStorage storage,
            int dogs, int enrolledToday) throws Exception {
        WatchDog before = new WatchDog(model.getWatchDog());
        var dogView = List.copyOf(model.getFilteredDogList());
        var customerView = List.copyOf(model.getFilteredCustomerList());
        var activityView = List.copyOf(model.getFilteredActivityList());

        LocalDate dateBeforeExecution = LocalDate.now();
        String feedback = logic.execute("summary").getFeedbackToUser();
        LocalDate dateAfterExecution = LocalDate.now();
        // Either date is valid if execution crosses midnight; expectations use the known fixture, not Activity APIs.
        assertTrue(List.of(expectedFeedback(dateBeforeExecution, dogs, enrolledToday),
                expectedFeedback(dateAfterExecution, dogs, enrolledToday)).contains(feedback),
                "Unexpected feedback returned by LogicManager: " + feedback);
        // Use the public clock seam for stable counts over the persisted, fixed-date bookings.
        assertEquals("Overall: " + dogs + " dogs | 1 customers\n"
                + "Activity schedules: 5 total | 4 from today onwards\n"
                + "Today (2026-10-24): 3 activities | " + enrolledToday + " distinct dogs enrolled",
                new SummaryCommand(CLOCK).execute(model).getFeedbackToUser());
        assertEquals(before, model.getWatchDog());
        assertEquals(dogView, model.getFilteredDogList());
        assertEquals(customerView, model.getFilteredCustomerList());
        assertEquals(activityView, model.getFilteredActivityList());
        assertEquals(before, storage.readWatchDog().orElseThrow());
    }

    private static String expectedFeedback(LocalDate today, int dogs, int enrolledOnFixtureDate) {
        List<LocalDate> oneTimeDates = List.of(DATE.minusDays(1), DATE, DATE.plusDays(1));
        long upcoming = 2 + oneTimeDates.stream().filter(date -> !date.isBefore(today)).count();
        long occurringToday = oneTimeDates.stream().filter(today::equals).count();
        if (!today.isBefore(DATE.minusDays(1))) {
            occurringToday++; // The daily schedule starts the day before the fixture date.
        }
        if (!today.isBefore(DATE) && today.getDayOfWeek() == DATE.getDayOfWeek()) {
            occurringToday++; // The weekly schedule starts on the fixture date.
        }
        int enrolledToday = 0;
        if (today.equals(DATE)) {
            enrolledToday = enrolledOnFixtureDate;
        } else if (today.equals(DATE.plusDays(1))) {
            enrolledToday = 1; // Dog 2's booking survives every deletion/undo/redo checkpoint.
        }
        return "Overall: " + dogs + " dogs | 1 customers\n"
                + "Activity schedules: 5 total | " + upcoming + " from today onwards\n"
                + "Today (" + today + "): " + occurringToday + " activities | " + enrolledToday
                + " distinct dogs enrolled";
    }

    private static WatchDog populatedBook() {
        WatchDog book = new WatchDog();
        for (int id : List.of(1, 2)) {
            book.addDogFromStorage(new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        }
        book.addCustomer(new Customer(1, "Alice Tan", "91234567", "alice@example.com", "98765432"));
        book.addActivity(new ActivityName("Past"), DATE.minusDays(1), LocalTime.of(6, 0),
                LocalTime.of(7, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Today"), DATE, LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Future"), DATE.plusDays(1), LocalTime.of(8, 0),
                LocalTime.of(9, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Daily"), DATE.minusDays(1), LocalTime.of(9, 0),
                LocalTime.of(10, 0), Recurrence.DAILY);
        book.addActivity(new ActivityName("Weekly"), DATE, LocalTime.of(10, 0),
                LocalTime.of(11, 0), Recurrence.WEEKLY);
        BOOKINGS.forEach(book::addEnrolmentFromStorage);
        return book;
    }
}
