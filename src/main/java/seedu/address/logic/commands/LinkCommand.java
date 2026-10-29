package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.LinkException;

/** Links a batch of dogs to a customer using persistent IDs. */
public class LinkCommand extends Command {
    public static final String COMMAND_WORD = "link";
    public static final String MESSAGE_USAGE = "link: Links one or more unowned dogs to a customer.\n"
            + "Parameters: c/CUSTOMER_ID d/DOG_ID [DOG_ID]...\n"
            + "Customer and dog IDs must be positive and must not repeat.\n"

            + "The entire batch is rejected if any record is missing or any dog already has an owner.\n"
            + "Example: link c/1 d/2 3";
    public static final String MESSAGE_SUCCESS = "Linked dog IDs %s to customer ID %d.";

    private final int customerId;
    private final List<Integer> dogIds;

    /** Creates a link request containing distinct positive dog IDs and a positive customer ID. */
    public LinkCommand(int customerId, List<Integer> dogIds) {
        this.dogIds = List.copyOf(dogIds);
        checkArgument(customerId >= 1 && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 1)

                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid link IDs.");
        this.customerId = customerId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.linkDogs(customerId, dogIds);
        } catch (LinkException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, customerId));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LinkCommand otherCommand
                && customerId == otherCommand.customerId && dogIds.equals(otherCommand.dogIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, dogIds);
    }
}
