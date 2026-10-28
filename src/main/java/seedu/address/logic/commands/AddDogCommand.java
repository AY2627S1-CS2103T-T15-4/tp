package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BREED;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SIZE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.Dog;

/** Adds a dog to the address book. */
public class AddDogCommand extends Command {

    public static final String COMMAND_WORD = "adddog";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a dog to WatchDog! "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_BREED + "BREED "
            + PREFIX_SIZE + "SIZE "
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Cookie "
            + PREFIX_BREED + "Golden Retriever "
            + PREFIX_SIZE + "Large\n"
            + "Valid sizes: small, medium, large, giant.";

    public static final String MESSAGE_FORMAT_GUIDANCE = "\nFormat: " + COMMAND_WORD + " "
            + PREFIX_NAME + "NAME " + PREFIX_BREED + "BREED " + PREFIX_SIZE + "SIZE"
            + "\nExample: " + COMMAND_WORD + " " + PREFIX_NAME + "Cookie "
            + PREFIX_BREED + "Golden Retriever " + PREFIX_SIZE + "Large"
            + "\nValid sizes: small, medium, large, giant.";

    public static final String MESSAGE_SUCCESS = "New dog added: %1$s";
    public static final String MESSAGE_DUPLICATE_DOG = "This dog already exists.";

    private final Dog toAdd;

    /** Creates an {@code AddDogCommand} to add the specified {@code Dog}. */
    public AddDogCommand(Dog dog) {
        requireNonNull(dog);
        toAdd = dog;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasDog(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_DOG);
        }
        Dog addedDog = model.addDog(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(addedDog)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddDogCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
