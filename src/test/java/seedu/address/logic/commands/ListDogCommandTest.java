package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link ListDogCommand}. */
public class ListDogCommandTest {

    @Test
    public void execute_returnsSuccessMessage() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        model.addDog(new Dog(new DogName("Leo"), new Breed("Poodle"), new Size("Large")));

        CommandResult result = new ListDogCommand().execute(model);

        assertEquals(ListDogCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
    }
}
