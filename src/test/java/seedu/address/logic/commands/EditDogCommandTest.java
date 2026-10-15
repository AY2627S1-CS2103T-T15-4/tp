package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditDogCommand.EditDogDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class EditDogCommandTest {
    @Test
    public void execute_existingDog_editsFieldsAndPreservesId() throws Exception {
        ModelManager model = new ModelManager();
        Dog original = model.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));
        EditDogDescriptor descriptor = new EditDogDescriptor();
        descriptor.setName(new DogName("Milo"));

        CommandResult result = new EditDogCommand(original.getId(), descriptor).execute(model);
        Dog edited = model.getDogList().get(0);

        assertEquals(original.getId(), edited.getId());
        assertEquals("Milo", edited.getName().toString());
        assertEquals("Golden Retriever", edited.getBreed().toString());
        assertEquals("Large", edited.getSize().toString());
        assertEquals(String.format(EditDogCommand.MESSAGE_EDIT_DOG_SUCCESS,
                seedu.address.logic.Messages.format(edited)), result.getFeedbackToUser());
    }

    @Test
    public void execute_nonexistentDogId_throwsCommandException() {
        ModelManager model = new ModelManager();
        EditDogDescriptor descriptor = new EditDogDescriptor();
        descriptor.setSize(new Size("Small"));

        assertThrows(CommandException.class, () ->
                new EditDogCommand(99, descriptor).execute(model));
    }

    @Test
    public void execute_noEditedFields_throwsCommandException() {
        ModelManager model = new ModelManager();
        Dog dog = model.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));

        assertThrows(CommandException.class, () ->
                new EditDogCommand(dog.getId(), new EditDogDescriptor()).execute(model));
    }
}
