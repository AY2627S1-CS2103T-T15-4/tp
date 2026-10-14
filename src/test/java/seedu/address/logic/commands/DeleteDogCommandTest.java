package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link DeleteDogCommand}. */
public class DeleteDogCommandTest {

    @Test
    public void execute_existingDog_deletesDog() throws Exception {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Dog addedDog = model.addDog(new Dog(new DogName("Leo"), new Breed("Poodle"), new Size("Large")));

        CommandResult result = new DeleteDogCommand(addedDog.getId()).execute(model);

        assertEquals("Successfully deleted dog 0: Leo; Breed: Poodle; Size: Large",
                result.getFeedbackToUser());
        assertEquals(0, model.getDogList().size());
    }

    @Test
    public void execute_missingDog_throwsCommandException() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());

        assertThrows(CommandException.class, DeleteDogCommand.MESSAGE_INVALID_ID, () ->
                new DeleteDogCommand(99).execute(model));
    }
}
