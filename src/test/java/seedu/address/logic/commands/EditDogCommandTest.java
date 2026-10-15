package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void execute_existingDog_editsBreedAndSize() throws Exception {
        ModelManager model = new ModelManager();
        Dog original = model.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));
        EditDogDescriptor descriptor = new EditDogDescriptor();
        descriptor.setBreed(new Breed("Poodle"));
        descriptor.setSize(new Size("Small"));

        new EditDogCommand(original.getId(), descriptor).execute(model);

        Dog edited = model.getDogList().get(0);
        assertEquals("Poodle", edited.getBreed().toString());
        assertEquals("Small", edited.getSize().toString());
    }

    @Test
    public void execute_noEditedFields_throwsCommandException() {
        ModelManager model = new ModelManager();
        Dog dog = model.addDog(new Dog(new DogName("Cookie"),
                new Breed("Golden Retriever"), new Size("Large")));

        assertThrows(CommandException.class, () ->
                new EditDogCommand(dog.getId(), new EditDogDescriptor()).execute(model));
    }

    @Test
    public void constructor_negativeId_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new EditDogCommand(-1, new EditDogDescriptor()));
    }

    @Test
    public void equals_sameDifferentAndOtherType_returnsExpectedResult() {
        EditDogDescriptor descriptor = new EditDogDescriptor();
        descriptor.setName(new DogName("Milo"));
        EditDogCommand command = new EditDogCommand(1, descriptor);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new EditDogCommand(1, descriptor)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("command"));
        assertFalse(command.equals(new EditDogCommand(2, descriptor)));
        assertFalse(command.equals(new EditDogCommand(1, new EditDogDescriptor())));
        assertTrue(command.toString().contains("dogId"));
    }

    @Test
    public void descriptor_fieldsAndEquality_behaveAsExpected() {
        EditDogDescriptor descriptor = new EditDogDescriptor();
        assertFalse(descriptor.isAnyFieldEdited());
        assertEquals(java.util.Optional.empty(), descriptor.getName());
        assertEquals(java.util.Optional.empty(), descriptor.getBreed());
        assertEquals(java.util.Optional.empty(), descriptor.getSize());

        descriptor.setName(new DogName("Milo"));
        descriptor.setBreed(new Breed("Beagle"));
        descriptor.setSize(new Size("Small"));
        EditDogDescriptor copy = new EditDogDescriptor(descriptor);
        assertTrue(descriptor.isAnyFieldEdited());
        assertEquals(descriptor, copy);
        assertTrue(descriptor.equals(descriptor));
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals("descriptor"));
        EditDogDescriptor different = new EditDogDescriptor();
        different.setBreed(new Breed("Beagle"));
        assertFalse(descriptor.equals(different));
        assertTrue(descriptor.toString().contains("name"));
    }
}
