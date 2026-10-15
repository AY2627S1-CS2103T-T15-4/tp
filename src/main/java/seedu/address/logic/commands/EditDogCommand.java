package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SIZE;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Edits the details of an existing dog in the address book. */
public class EditDogCommand extends Command {
    public static final String COMMAND_WORD = "editdog";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the dog identified by its ID. "
            + "Parameters: ID [" + PREFIX_NAME + "NAME] [" + PREFIX_BREED + "BREED] ["
            + PREFIX_SIZE + "SIZE]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_SIZE + "Large";
    public static final String MESSAGE_EDIT_DOG_SUCCESS = "Edited dog: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_INVALID_DOG_ID = "The dog ID provided is invalid.";

    private final int dogId;
    private final EditDogDescriptor editDogDescriptor;

    /** Creates a command to edit the dog with {@code dogId}. */
    public EditDogCommand(int dogId, EditDogDescriptor editDogDescriptor) {
        if (dogId < 0) {
            throw new IllegalArgumentException("Dog ID must be nonnegative.");
        }
        requireNonNull(editDogDescriptor);
        this.dogId = dogId;
        this.editDogDescriptor = new EditDogDescriptor(editDogDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Dog dogToEdit = model.getDogList().stream()
                .filter(dog -> dog.getId() == dogId)
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_INVALID_DOG_ID));

        if (!editDogDescriptor.isAnyFieldEdited()) {
            throw new CommandException(MESSAGE_NOT_EDITED);
        }

        Dog editedDog = createEditedDog(dogToEdit, editDogDescriptor);
        model.setDog(dogToEdit, editedDog);
        return new CommandResult(String.format(MESSAGE_EDIT_DOG_SUCCESS, Messages.format(editedDog)));
    }

    private static Dog createEditedDog(Dog dogToEdit, EditDogDescriptor descriptor) {
        DogName updatedName = descriptor.getName().orElse(dogToEdit.getName());
        Breed updatedBreed = descriptor.getBreed().orElse(dogToEdit.getBreed());
        Size updatedSize = descriptor.getSize().orElse(dogToEdit.getSize());
        return new Dog(dogToEdit.getId(), updatedName, updatedBreed, updatedSize);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EditDogCommand otherEditDogCommand)) {
            return false;
        }
        return dogId == otherEditDogCommand.dogId
                && editDogDescriptor.equals(otherEditDogCommand.editDogDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dogId", dogId)
                .add("editDogDescriptor", editDogDescriptor)
                .toString();
    }

    /** Stores the non-empty dog fields to edit. */
    public static class EditDogDescriptor {
        private DogName name;
        private Breed breed;
        private Size size;

        public EditDogDescriptor() {}

        /** Creates a defensive copy of the given descriptor. */
        public EditDogDescriptor(EditDogDescriptor toCopy) {
            requireNonNull(toCopy);
            name = toCopy.name;
            breed = toCopy.breed;
            size = toCopy.size;
        }

        /** Returns true if at least one field is edited. */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, breed, size);
        }

        public void setName(DogName name) {
            this.name = name;
        }

        public Optional<DogName> getName() {
            return Optional.ofNullable(name);
        }

        public void setBreed(Breed breed) {
            this.breed = breed;
        }

        public Optional<Breed> getBreed() {
            return Optional.ofNullable(breed);
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public Optional<Size> getSize() {
            return Optional.ofNullable(size);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof EditDogDescriptor otherDescriptor)) {
                return false;
            }
            return Objects.equals(name, otherDescriptor.name)
                    && Objects.equals(breed, otherDescriptor.breed)
                    && Objects.equals(size, otherDescriptor.size);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("breed", breed)
                    .add("size", size)
                    .toString();
        }
    }
}
