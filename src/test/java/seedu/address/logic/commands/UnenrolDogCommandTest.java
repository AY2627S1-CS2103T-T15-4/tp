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
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link UnenrolDogCommand}. */
public class UnenrolDogCommandTest {
    private ModelManager modelWithEnrolment() throws Exception {
        AddressBook book = new AddressBook();
        book.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        book.addDogFromStorage(new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        book.enrolDogs(1, LocalDate.of(2028, 1, 1), List.of(1));
        return new ModelManager(book, new UserPrefs());
    }

    @Test
    public void execute_validEnrolment_removesEnrolment() throws Exception {
        ModelManager model = modelWithEnrolment();

        CommandResult result = new UnenrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(1)).execute(model);

        assertEquals("Unenrolled dog IDs [1] from activity ID 1 on 2028-01-01.", result.getFeedbackToUser());
        assertEquals(List.of(), model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void execute_missingEnrolment_throwsCommandException() throws Exception {
        ModelManager model = new ModelManager();
        CommandException exception = assertThrows(CommandException.class, () ->
                new UnenrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(1)).execute(model));

        assertEquals("Activity ID 1 does not exist." + UnenrolDogCommand.MESSAGE_FORMAT_GUIDANCE,
                exception.getMessage());
    }

    @Test
    public void execute_pastDate_removesHistoricalEnrolment() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        AddressBook book = new AddressBook();
        book.addActivity(new ActivityName("Walk"), yesterday,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        book.addDogFromStorage(new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")));
        book.addEnrolmentFromStorage(new Enrolment(1, 1, yesterday));
        ModelManager model = new ModelManager(book, new UserPrefs());

        CommandResult result = new UnenrolDogCommand(1, yesterday, List.of(1)).execute(model);

        assertEquals("Unenrolled dog IDs [1] from activity ID 1 on " + yesterday + ".",
                result.getFeedbackToUser());
        assertEquals(List.of(), model.getAddressBook().getEnrolmentList());
    }

    @Test
    public void constructorAndEquals_validateAndCompareFields() {
        UnenrolDogCommand command = new UnenrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2, 3));
        assertEquals(command, command);
        assertEquals(command, new UnenrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2, 3)));
        assertNotEquals(command, new UnenrolDogCommand(2, LocalDate.of(2028, 1, 1), List.of(2, 3)));
        assertNotEquals(command, new UnenrolDogCommand(1, LocalDate.of(2028, 1, 2), List.of(2, 3)));
        assertNotEquals(command, new UnenrolDogCommand(1, LocalDate.of(2028, 1, 1), List.of(2)));
        assertNotEquals(command, null);
        assertNotEquals(command, "command");
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(-1,
                LocalDate.of(2028, 1, 1), List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(0,
                LocalDate.of(2028, 1, 1), List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(1, null, List.of(2)));
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of(-2)));
        assertThrows(IllegalArgumentException.class, () -> new UnenrolDogCommand(1,
                LocalDate.of(2028, 1, 1), List.of(2, 2)));
    }
}
