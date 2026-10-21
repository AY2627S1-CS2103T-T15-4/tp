package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

/** Tests deleting an activity and its enrolments. */
public class DeleteActivityCommandTest {
    private ModelManager model;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
        book.addActivity(new ActivityName("Grooming"), LocalDate.of(2028, 2, 29),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        book.addDogFromStorage(new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        book.enrolDogs(1, List.of(7));
        book.enrolDogs(2, List.of(7));
        model = new ModelManager(book, new UserPrefs());
    }

    @Test
    public void parseAndExecute_existingRecurringActivity_removesOnlyItsEnrolments() throws Exception {
        Activity other = model.getActivityList().get(1);

        CommandResult result = new AddressBookParser().parseCommand("deleteactivity 1").execute(model);

        assertEquals("Successfully deleted activity 1: Walk", result.getFeedbackToUser());
        assertEquals(List.of(other), model.getActivityList());
        assertEquals(List.of(new Enrolment(2, 7)), model.getAddressBook().getEnrolmentList());
        assertEquals(7, model.getDogList().get(0).getId());
        assertEquals(3, model.addActivity(new ActivityName("Training"), LocalDate.of(2028, 3, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE).getId());
    }

    @Test
    public void execute_missingActivity_changesNothing() {
        List<Activity> originalActivities = List.copyOf(model.getActivityList());
        List<Enrolment> originalEnrolments = List.copyOf(model.getAddressBook().getEnrolmentList());

        assertThrows(CommandException.class, () -> new DeleteActivityCommand(99).execute(model));

        assertEquals(originalActivities, model.getActivityList());
        assertEquals(originalEnrolments, model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void removeActivity_staleActivity_doesNotRemoveEnrolments() {
        AddressBook book = new AddressBook(model.getAddressBook());
        Activity stale = book.getActivityList().get(0).withName(new ActivityName("Old name"));

        assertThrows(IllegalArgumentException.class, () -> book.removeActivity(stale));

        assertEquals(2, book.getActivityList().size());
        assertEquals(List.of(new Enrolment(1, 7), new Enrolment(2, 7)), book.getEnrolmentList());
    }

    @Test
    public void constructor_negativeId_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeleteActivityCommand(-1));
    }
}
