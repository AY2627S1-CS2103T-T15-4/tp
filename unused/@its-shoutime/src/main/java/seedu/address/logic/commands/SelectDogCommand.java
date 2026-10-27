package seedu.address.logic.commands;
//@@author its-shoutime-unused
// Archived because select dog is not part of the final product.

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.Dog;

/** Shows one dog, its owner, and that dog's current and future activities. */
public class SelectDogCommand extends Command {

    public static final String COMMAND_WORD = "select";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " dog <id>: Shows the dog, its owner, and its current and future activities.\n"
            + "Example: " + COMMAND_WORD + " dog 21";
    public static final String MESSAGE_SUCCESS = "Selected dog %1$d: %2$s";
    public static final String MESSAGE_INVALID_ID = "No such dog. Input a valid dog id";

    private static final Logger logger = LogsCenter.getLogger(SelectDogCommand.class);

    private final int dogId;

    /** Creates a command that selects the dog with the specified ID. */
    public SelectDogCommand(int dogId) {
        assert dogId >= 1 : "Dog ID must be positive";
        this.dogId = dogId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Dog selectedDog = model.getDogList().stream()
                .filter(dog -> dog.getId() == dogId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("No dog found with ID " + dogId);
                    return new CommandException(MESSAGE_INVALID_ID);
                });

        model.selectDog(selectedDog.getId());
        logger.info("Selected dog ID " + selectedDog.getId());
        return new CommandResult(String.format(MESSAGE_SUCCESS, selectedDog.getId(), selectedDog.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SelectDogCommand otherCommand && dogId == otherCommand.dogId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("dogId", dogId).toString();
    }
}
