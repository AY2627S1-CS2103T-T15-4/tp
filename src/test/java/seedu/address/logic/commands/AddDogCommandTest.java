package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link AddDogCommand}. */
public class AddDogCommandTest {
    private static final Dog DOG = new Dog(new DogName("Cookie"),
            new Breed("Poodle"), new Size("Large"));

    @Test
    public void execute_validDog_addsDog() throws Exception {
        ModelManager model = new ModelManager();
        CommandResult result = new AddDogCommand(DOG).execute(model);

        assertEquals(1, model.getDogList().size());
        assertEquals(1, model.getDogList().get(0).getId());
        assertTrue(result.getFeedbackToUser().contains("Cookie"));
    }

    @Test
    public void execute_duplicateDog_throwsCommandException() throws Exception {
        ModelManager model = new ModelManager();
        Dog existing = model.addDog(new Dog(new DogName("Cookie"),
                new Breed("Poodle"), new Size("Large")));

        assertThrows(CommandException.class, () -> new AddDogCommand(existing).execute(model));
    }

    @Test
    public void execute_exhaustedDogIds_throwsCommandExceptionWithoutMutation() {
        WatchDog watchDog = new WatchDog();
        watchDog.setNextDogId((long) Integer.MAX_VALUE + 1);
        ModelManager model = new ModelManager(watchDog, new UserPrefs());

        assertThrows(CommandException.class, "No more dog IDs are available.", () ->
                new AddDogCommand(DOG).execute(model));
        assertTrue(model.getDogList().isEmpty());
        assertEquals((long) Integer.MAX_VALUE + 1, model.getWatchDog().getNextDogId());
    }

    @Test
    public void equals_sameDifferentAndOtherType_returnsExpectedResult() {
        AddDogCommand command = new AddDogCommand(DOG);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new AddDogCommand(DOG)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("command"));
        assertFalse(command.equals(new AddDogCommand(new Dog(new DogName("Milo"),
                new Breed("Beagle"), new Size("Small")))));
        assertTrue(command.toString().contains("toAdd"));
    }
}
