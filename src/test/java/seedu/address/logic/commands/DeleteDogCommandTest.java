// @@author XanderGoh
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link DeleteDogCommand}. */
public class DeleteDogCommandTest {

    @Test
    public void execute_existingDog_deletesDog() throws Exception {
        Model model = new ModelManager(new WatchDog(), new UserPrefs());
        Dog addedDog = model.addDog(new Dog(new DogName("Leo"), new Breed("Poodle"), new Size("Large")));

        CommandResult result = new DeleteDogCommand(addedDog.getId()).execute(model);

        assertEquals("Successfully deleted dog 1: Leo; Breed: Poodle; Size: Large",
                result.getFeedbackToUser());
        assertEquals(0, model.getDogList().size());
    }

    @Test
    public void execute_secondDog_deletesMatchingDog() throws Exception {
        Model model = new ModelManager(new WatchDog(), new UserPrefs());
        model.addDog(new Dog(new DogName("Leo"), new Breed("Poodle"), new Size("Large")));
        Dog secondDog = model.addDog(new Dog(new DogName("Milo"), new Breed("Beagle"), new Size("Small")));

        new DeleteDogCommand(secondDog.getId()).execute(model);

        assertEquals(1, model.getDogList().size());
        assertEquals("Leo", model.getDogList().get(0).getName().fullName);
    }

    @Test
    public void execute_dogWithEnrolments_deletesOnlyThatDogsEnrolments() throws Exception {
        WatchDog watchDog = new WatchDog();
        Dog dogToDelete = watchDog.addDog(new Dog(new DogName("Leo"), new Breed("Poodle"), new Size("Large")));
        Dog retainedDog = watchDog.addDog(new Dog(new DogName("Milo"), new Breed("Beagle"), new Size("Small")));
        LocalDate pastDate = LocalDate.now().minusDays(14);
        LocalDate firstFutureDate = LocalDate.now().plusDays(7);
        LocalDate secondFutureDate = firstFutureDate.plusDays(7);
        watchDog.addActivity(new ActivityName("Walk"), pastDate, LocalTime.of(9, 0), LocalTime.of(10, 0),
                Recurrence.WEEKLY);
        watchDog.addEnrolmentFromStorage(new Enrolment(1, dogToDelete.getId(), pastDate));
        watchDog.addEnrolmentFromStorage(new Enrolment(1, retainedDog.getId(), pastDate));
        watchDog.enrolDogs(1, firstFutureDate, List.of(dogToDelete.getId(), retainedDog.getId()));
        watchDog.enrolDogs(1, secondFutureDate, List.of(dogToDelete.getId(), retainedDog.getId()));
        Model model = new ModelManager(watchDog, new UserPrefs());

        new DeleteDogCommand(dogToDelete.getId()).execute(model);

        assertEquals(List.of(retainedDog), model.getDogList());
        assertEquals(3, model.getWatchDog().getEnrolmentList().size());
        assertTrue(model.getWatchDog().getEnrolmentList().stream()
                .allMatch(enrolment -> enrolment.getDogId() == retainedDog.getId()));
    }

    @Test
    public void execute_missingDog_throwsCommandException() {
        Model model = new ModelManager(new WatchDog(), new UserPrefs());

        assertThrows(CommandException.class, DeleteDogCommand.MESSAGE_INVALID_ID, () ->
                new DeleteDogCommand(99).execute(model));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteDogCommand(12).execute(null));
    }

    @Test
    public void equals_sameId_returnsTrue() {
        assertTrue(new DeleteDogCommand(12).equals(new DeleteDogCommand(12)));
    }

    @Test
    public void equals_sameObject_returnsTrue() {
        DeleteDogCommand command = new DeleteDogCommand(12);
        assertTrue(command.equals(command));
    }

    @Test
    public void equals_differentId_returnsFalse() {
        assertFalse(new DeleteDogCommand(12).equals(new DeleteDogCommand(13)));
    }

    @Test
    public void equals_null_returnsFalse() {
        assertFalse(new DeleteDogCommand(12).equals(null));
    }

    @Test
    public void equals_differentType_returnsFalse() {
        assertFalse(new DeleteDogCommand(12).equals(new Object()));
    }

    @Test
    public void toString_containsDogId() {
        assertEquals("seedu.address.logic.commands.DeleteDogCommand{dogId=12}",
                new DeleteDogCommand(12).toString());
    }
}
