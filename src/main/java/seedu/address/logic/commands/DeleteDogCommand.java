// @@author XanderGoh
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.Dog;

/** Deletes a dog identified by its persistent ID. */
public class DeleteDogCommand extends Command {

    public static final String COMMAND_WORD = "deletedog";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " <id>: Deletes the dog with the specified ID.\n"
            + "Example: deletedog 12";
    public static final String MESSAGE_SUCCESS = "Successfully deleted dog %1$d: %2$s; Breed: %3$s; Size: %4$s";
    public static final String MESSAGE_INVALID_ID = "Error in deleting dog:\nPlease enter a valid dog ID";

    private final int dogId;

    /**
     * Creates a command to delete the dog with the specified positive ID.
     *
     * @throws IllegalArgumentException if {@code dogId} is not positive
     */
    public DeleteDogCommand(int dogId) {
        checkArgument(dogId >= 1, "Dog ID must be positive.");
        this.dogId = dogId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Dog dogToDelete = model.getDogList().stream()
                .filter(dog -> dog.getId() == dogId)
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_INVALID_ID));
        model.deleteDog(dogToDelete);
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogToDelete.getId(), dogToDelete.getName(),
                dogToDelete.getBreed(), dogToDelete.getSize()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeleteDogCommand otherCommand && dogId == otherCommand.dogId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("dogId", dogId).toString();
    }
}
