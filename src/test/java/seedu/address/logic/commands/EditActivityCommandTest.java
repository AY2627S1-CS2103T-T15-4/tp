package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditActivityCommand.EditActivityDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests editing an activity series and its enrolments. */
public class EditActivityCommandTest {
    private ModelManager model;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        addressBook.addActivity(new ActivityName("Grooming"), LocalDate.of(2028, 2, 29),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        addressBook.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        addressBook.enrolDogs(1, List.of(7));
        addressBook.enrolDogs(2, List.of(7));
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_dateChange_removesOnlyTargetActivityEnrolments() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setDate(LocalDate.of(2028, 3, 1));

        CommandResult result = new EditActivityCommand(1, descriptor).execute(model);

        assertEquals("Edited activity ID 1. Enrolments removed: 1.", result.getFeedbackToUser());
        assertEquals(LocalDate.of(2028, 3, 1), model.getActivityList().get(0).getStartDate());
        assertEquals(List.of(new Enrolment(2, 7)), model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_timeChange_preservesEnrolments() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setStartTime(LocalTime.of(8, 0));

        new EditActivityCommand(1, descriptor).execute(model);

        assertEquals(LocalTime.of(8, 0), model.getActivityList().get(0).getStartTime());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_timeChangeOverlapsAnotherBooking_rejectsWithoutChangingData() {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 30));
        Activity original = model.getActivityList().get(0);
        String conflictMessage = "Dog ID 7 has an overlapping booking in activity ID 2.";
        EditActivityCommand command = new EditActivityCommand(1, descriptor);

        assertThrows(CommandException.class, conflictMessage, () -> command.execute(model));

        assertEquals(original, model.getActivityList().get(0));
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_timeChangeEndsWhenAnotherBookingStarts_succeeds() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 0));

        new EditActivityCommand(1, descriptor).execute(model);

        assertEquals(LocalTime.of(11, 0), model.getActivityList().get(0).getEndTime());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_timeChangeOverlapsLaterWeeklyBooking_rejects() throws Exception {
        model.unenrolDogs(2, List.of(7));
        Activity later = model.addActivity(new ActivityName("Training"), LocalDate.of(2028, 3, 7),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.WEEKLY);
        model.enrolDogs(later.getId(), List.of(7));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 30));

        assertThrows(CommandException.class, () -> new EditActivityCommand(1, descriptor).execute(model));

        assertEquals(LocalTime.of(10, 0), model.getActivityList().get(0).getEndTime());
    }

    @Test
    public void execute_timeChangeOverlapsLaterDailyBooking_rejects() throws Exception {
        model.unenrolDogs(2, List.of(7));
        Activity daily = model.addActivity(new ActivityName("Training"), LocalDate.of(2028, 3, 1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.DAILY);
        model.enrolDogs(daily.getId(), List.of(7));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 30));

        assertThrows(CommandException.class, () -> new EditActivityCommand(1, descriptor).execute(model));

        assertEquals(LocalTime.of(10, 0), model.getActivityList().get(0).getEndTime());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(daily.getId(), 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_sameDateAndConflictingTime_rejectsWithoutUnenrolling() {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setDate(LocalDate.of(2028, 2, 29));
        descriptor.setEndTime(LocalTime.of(11, 30));

        assertThrows(CommandException.class, () -> new EditActivityCommand(1, descriptor).execute(model));

        assertEquals(LocalTime.of(10, 0), model.getActivityList().get(0).getEndTime());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_recurrenceChangeRemovesEnrolmentsBeforeConflictCheck() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setRecurrence(Recurrence.DAILY);
        descriptor.setEndTime(LocalTime.of(11, 30));

        CommandResult result = new EditActivityCommand(1, descriptor).execute(model);

        assertEquals("Edited activity ID 1. Enrolments removed: 1.", result.getFeedbackToUser());
        assertEquals(Recurrence.DAILY, model.getActivityList().get(0).getRecurrence());
        assertEquals(List.of(new Enrolment(2, 7)), model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_dateChangeRemovesEnrolmentsBeforeConflictCheck() throws Exception {
        model.unenrolDogs(2, List.of(7));
        Activity daily = model.addActivity(new ActivityName("Training"), LocalDate.of(2028, 2, 29),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.DAILY);
        model.enrolDogs(daily.getId(), List.of(7));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setDate(LocalDate.of(2028, 3, 1));
        descriptor.setEndTime(LocalTime.of(11, 30));

        new EditActivityCommand(1, descriptor).execute(model);

        assertEquals(LocalDate.of(2028, 3, 1), model.getActivityList().get(0).getStartDate());
        assertEquals(List.of(new Enrolment(daily.getId(), 7)), model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void parseAndExecute_conflictingEdit_reportsCommandErrorWithoutChangingData() throws Exception {
        Activity original = model.getActivityList().get(0);
        Command command = new AddressBookParser().parseCommand("editactivity 1 end/11:30");
        String conflictMessage = "Dog ID 7 has an overlapping booking in activity ID 2.";

        assertThrows(CommandException.class, conflictMessage, () -> command.execute(model));

        assertEquals(original, model.getActivityList().get(0));
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_timeChangeOverlapsAnotherActivityOnDifferentWeekday_succeeds() throws Exception {
        model.unenrolDogs(2, List.of(7));
        Activity other = model.addActivity(new ActivityName("Training"), LocalDate.of(2028, 3, 1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.WEEKLY);
        model.enrolDogs(other.getId(), List.of(7));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 30));

        new EditActivityCommand(1, descriptor).execute(model);

        assertEquals(LocalTime.of(11, 30), model.getActivityList().get(0).getEndTime());
    }

    @Test
    public void execute_timeChangeOverlapsActivityWithDifferentDog_succeeds() throws Exception {
        model.unenrolDogs(2, List.of(7));
        Dog otherDog = model.addDog(new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("Small")));
        model.enrolDogs(2, List.of(otherDog.getId()));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(11, 30));

        new EditActivityCommand(1, descriptor).execute(model);

        assertEquals(LocalTime.of(11, 30), model.getActivityList().get(0).getEndTime());
    }

    @Test
    public void execute_invalidTimeRange_changesNothing() {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setEndTime(LocalTime.of(8, 0));
        Activity original = model.getActivityList().get(0);

        assertThrows(CommandException.class, () -> new EditActivityCommand(1, descriptor).execute(model));

        assertEquals(original, model.getActivityList().get(0));
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_noFieldsOrUnknownId_rejectsWithoutChangingData() {
        EditActivityDescriptor empty = new EditActivityDescriptor();
        EditActivityDescriptor renamed = new EditActivityDescriptor();
        renamed.setName(new ActivityName("Training"));
        List<Activity> originalActivities = List.copyOf(model.getActivityList());

        assertThrows(CommandException.class, EditActivityCommand.MESSAGE_NOT_EDITED, () ->
                new EditActivityCommand(1, empty).execute(model));
        assertThrows(CommandException.class, EditActivityCommand.MESSAGE_INVALID_ACTIVITY_ID, () ->
                new EditActivityCommand(99, renamed).execute(model));
        assertEquals(originalActivities, model.getActivityList());
    }

    @Test
    public void execute_renameCompletedActivity_preservesScheduleCompletionAndEnrolments() throws Exception {
        model.markActivity(1);
        Activity original = model.getActivityList().get(0);
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));

        CommandResult result = new EditActivityCommand(1, descriptor).execute(model);

        Activity edited = model.getActivityList().get(0);
        assertEquals("Edited activity ID 1. Enrolments removed: 0.", result.getFeedbackToUser());
        assertEquals(new ActivityName("Training"), edited.getName());
        assertEquals(original.getStartDate(), edited.getStartDate());
        assertEquals(original.getStartTime(), edited.getStartTime());
        assertEquals(original.getEndTime(), edited.getEndTime());
        assertEquals(original.getRecurrence(), edited.getRecurrence());
        assertTrue(edited.isCompleted());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)),
                model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void constructor_defensivelyCopiesDescriptor() throws Exception {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));
        EditActivityCommand command = new EditActivityCommand(1, descriptor);
        descriptor.setName(new ActivityName("Changed later"));

        command.execute(model);

        assertEquals(new ActivityName("Training"), model.getActivityList().get(0).getName());
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new EditActivityCommand(-1, new EditActivityDescriptor()));
        assertThrows(NullPointerException.class, () -> new EditActivityCommand(1, null));
    }

    @Test
    public void equals_sameFieldsAndDifferentFields() {
        EditActivityDescriptor first = new EditActivityDescriptor();
        first.setName(new ActivityName("Training"));
        EditActivityDescriptor same = new EditActivityDescriptor(first);
        EditActivityDescriptor different = new EditActivityDescriptor();
        different.setDate(LocalDate.of(2028, 3, 1));

        assertEquals(first, same);
        assertNotEquals(first, different);
        assertEquals(new EditActivityCommand(1, first), new EditActivityCommand(1, same));
        assertNotEquals(new EditActivityCommand(1, first), new EditActivityCommand(2, same));
        assertNotEquals(new EditActivityCommand(1, first), new EditActivityCommand(1, different));
    }
}
