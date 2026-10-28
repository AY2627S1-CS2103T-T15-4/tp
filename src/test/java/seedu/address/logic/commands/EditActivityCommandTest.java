package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private static final LocalDate INITIAL_DATE = LocalDate.of(2028, 2, 29);
    private ModelManager model;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Walk"), INITIAL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        addressBook.addActivity(new ActivityName("Grooming"), INITIAL_DATE,
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        addressBook.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        addressBook.enrolDogs(1, INITIAL_DATE, List.of(7));
        addressBook.enrolDogs(2, INITIAL_DATE, List.of(7));
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_enrolledActivityWithDateChange_rejectsWithoutChangingData() {
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setDate(LocalDate.of(2028, 3, 1));
        Activity original = model.getActivityList().get(0);
        List<Enrolment> originalEnrolments = List.copyOf(model.getAddressBook().getEnrolmentList());

        String expectedMessage = EditActivityCommand.MESSAGE_ACTIVITY_HAS_ENROLMENTS.formatted(1);
        assertThrows(CommandException.class, expectedMessage, () ->
                new EditActivityCommand(1, descriptor).execute(model));

        assertEquals(original, model.getActivityList().get(0));
        assertEquals(originalEnrolments, model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_enrolledActivityWithNameOrTimeChange_rejects() {
        EditActivityDescriptor rename = new EditActivityDescriptor();
        rename.setName(new ActivityName("Training"));
        EditActivityDescriptor timeChange = new EditActivityDescriptor();
        timeChange.setStartTime(LocalTime.of(8, 0));

        String expectedMessage = EditActivityCommand.MESSAGE_ACTIVITY_HAS_ENROLMENTS.formatted(1);
        assertThrows(CommandException.class, expectedMessage, () ->
                new EditActivityCommand(1, rename).execute(model));
        assertThrows(CommandException.class, expectedMessage, () ->
                new EditActivityCommand(1, timeChange).execute(model));
    }

    @Test
    public void parseAndExecute_enrolledActivity_suggestsCreatingNewActivity() throws Exception {
        Command command = new AddressBookParser().parseCommand("editactivity 1 start/08:00");

        String expectedMessage = EditActivityCommand.MESSAGE_ACTIVITY_HAS_ENROLMENTS.formatted(1);
        assertThrows(CommandException.class, expectedMessage, () -> command.execute(model));
    }

    @Test
    public void execute_activityWithoutEnrolments_allowsScheduleAndNameChanges() throws Exception {
        ModelManager emptyModel = createModelWithoutEnrolments();
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));
        descriptor.setDate(LocalDate.of(2028, 3, 1));
        descriptor.setStartTime(LocalTime.of(8, 0));
        descriptor.setEndTime(LocalTime.of(9, 0));
        descriptor.setRecurrence(Recurrence.DAILY);

        CommandResult result = new EditActivityCommand(1, descriptor).execute(emptyModel);

        assertEquals("Edited activity ID 1.", result.getFeedbackToUser());
        Activity edited = emptyModel.getActivityList().get(0);
        assertEquals(new ActivityName("Training"), edited.getName());
        assertEquals(LocalDate.of(2028, 3, 1), edited.getStartDate());
        assertEquals(LocalTime.of(8, 0), edited.getStartTime());
        assertEquals(LocalTime.of(9, 0), edited.getEndTime());
        assertEquals(Recurrence.DAILY, edited.getRecurrence());
        assertTrue(emptyModel.getAddressBook().getEnrolmentList().isEmpty());
    }

    @Test
    public void execute_onlyOtherActivityHasEnrolments_editsTargetAndPreservesEnrolments() throws Exception {
        model.unenrolDogs(1, INITIAL_DATE, List.of(7));
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setRecurrence(Recurrence.DAILY);
        descriptor.setDate(INITIAL_DATE.plusDays(1));

        CommandResult result = new EditActivityCommand(1, descriptor).execute(model);

        assertEquals("Edited activity ID 1.", result.getFeedbackToUser());
        assertEquals(INITIAL_DATE.plusDays(1), model.getActivityList().get(0).getStartDate());
        assertEquals(Recurrence.DAILY, model.getActivityList().get(0).getRecurrence());
        assertEquals(List.of(new Enrolment(2, 7, INITIAL_DATE)), model.getAddressBook().getEnrolmentList());
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
    public void constructor_defensivelyCopiesDescriptor() throws Exception {
        ModelManager emptyModel = createModelWithoutEnrolments();
        EditActivityDescriptor descriptor = new EditActivityDescriptor();
        descriptor.setName(new ActivityName("Training"));
        EditActivityCommand command = new EditActivityCommand(1, descriptor);
        descriptor.setName(new ActivityName("Changed later"));

        command.execute(emptyModel);

        assertEquals(new ActivityName("Training"), emptyModel.getActivityList().get(0).getName());
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
        EditActivityCommand command = new EditActivityCommand(1, first);
        assertEquals(command, command);
        assertEquals(command, new EditActivityCommand(1, same));
        assertNotEquals(new EditActivityCommand(1, first), new EditActivityCommand(2, same));
        assertNotEquals(new EditActivityCommand(1, first), new EditActivityCommand(1, different));
        assertNotEquals(command, null);
        assertNotEquals(command, "command");
    }

    @Test
    public void descriptor_equalsEachFieldAndToString() {
        EditActivityDescriptor full = new EditActivityDescriptor();
        full.setName(new ActivityName("Training"));
        full.setDate(LocalDate.of(2028, 3, 1));
        full.setStartTime(LocalTime.of(8, 30));
        full.setEndTime(LocalTime.of(9, 30));
        full.setRecurrence(Recurrence.DAILY);

        EditActivityDescriptor same = new EditActivityDescriptor(full);
        assertEquals(full, same);
        assertEquals(full, full);
        assertFalse(full.equals(null));
        assertFalse(full.equals("descriptor"));

        EditActivityDescriptor differentName = new EditActivityDescriptor(full);
        differentName.setName(new ActivityName("Walk"));
        EditActivityDescriptor differentDate = new EditActivityDescriptor(full);
        differentDate.setDate(LocalDate.of(2028, 3, 2));
        EditActivityDescriptor differentStart = new EditActivityDescriptor(full);
        differentStart.setStartTime(LocalTime.of(8, 0));
        EditActivityDescriptor differentEnd = new EditActivityDescriptor(full);
        differentEnd.setEndTime(LocalTime.of(10, 0));
        EditActivityDescriptor differentRecurrence = new EditActivityDescriptor(full);
        differentRecurrence.setRecurrence(Recurrence.WEEKLY);

        assertNotEquals(full, differentName);
        assertNotEquals(full, differentDate);
        assertNotEquals(full, differentStart);
        assertNotEquals(full, differentEnd);
        assertNotEquals(full, differentRecurrence);
        assertTrue(full.toString().contains("Training"));
        assertTrue(new EditActivityCommand(1, full).toString().contains("activityId=1"));
    }

    private static ModelManager createModelWithoutEnrolments() throws Exception {
        AddressBook addressBook = new AddressBook();
        addressBook.addActivity(new ActivityName("Walk"), INITIAL_DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        return new ModelManager(addressBook, new UserPrefs());
    }
}
