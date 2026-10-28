package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link EnrolDogCommand}. */
public class EnrolDogCommandTest {
    @Test
    public void execute_validDogs_enrolsDogs() throws Exception {
        AddressBook book = new AddressBook();
        book.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        book.addDogFromStorage(new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        ModelManager model = new ModelManager(book, new UserPrefs());

        CommandResult result = new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(1)).execute(model);

        assertEquals("Enrolled dog IDs [1] to activity ID 1 on 2028-01-01.", result.getFeedbackToUser());
        assertEquals(1, model.getAddressBook().getEnrolmentList().size());
    }

    @Test
    public void execute_invalidActivityOrDog_throwsCommandException() throws Exception {
        ModelManager model = new ModelManager();
        CommandException exception = assertThrows(CommandException.class, () ->
                new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(1)).execute(model));

        assertEquals("Activity ID 1 does not exist." + EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE,
                exception.getMessage());
    }

    @Test
    public void execute_pastDate_returnsSpecificMessageWithGuidance() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        ModelManager model = new ModelManager();

        CommandException exception = assertThrows(CommandException.class, () ->
                new EnrolDogCommand(1, yesterday, List.of(1)).execute(model));

        assertEquals("Cannot enrol on " + yesterday + ": the date must be today or later. Today is "
                + today + "." + EnrolDogCommand.MESSAGE_FORMAT_GUIDANCE, exception.getMessage());
    }

    @Test
    public void execute_duplicateEnrolment_throwsCommandException() throws Exception {
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(1)).execute(model);

        assertThrows(CommandException.class, () -> new EnrolDogCommand(1, LocalDate.of(2028, 1, 1),
                List.of(1)).execute(model));
    }

    @Test
    public void constructorAndEquals_validateAndCompareFields() {
        EnrolDogCommand command = new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2, 3));
        assertEquals(command, command);
        assertEquals(command, new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2, 3)));
        assertNotEquals(command, new EnrolDogCommand(2, LocalDate.of(2028, 1, 1), List.of(2, 3)));
        assertNotEquals(command, new EnrolDogCommand(1, LocalDate.of(2028, 1, 2), List.of(2, 3)));
        assertNotEquals(command, new EnrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2)));
        assertNotEquals(command, null);
        assertNotEquals(command, "command");
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(-1,
                LocalDate.of(2028, 1, 1), List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(0,
                LocalDate.of(2028, 1, 1), List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(1, null, List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of(-2)));
        assertThrows(IllegalArgumentException.class, () -> new EnrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of(2, 2)));
    }
}
