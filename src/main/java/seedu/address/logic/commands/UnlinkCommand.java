package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.UnlinkException;

/** Unlinks a batch of dogs from a customer using persistent IDs. */
public class UnlinkCommand extends Command {
    public static final String COMMAND_WORD = "unlink";
    public static final String MESSAGE_USAGE = "unlink: Unlinks one or more dogs from their customer.\n"
            + "Parameters: c/CUSTOMER_ID d/DOG_ID [d/DOG_ID]...\n"
            + "IDs must be integers from 0 to 2147483647; dog IDs must not repeat.\n"
            + "The entire batch is rejected if any record is missing or any dog is not owned by this customer.\n"
            + "Example: unlink c/1 d/2 d/3";
    public static final String MESSAGE_SUCCESS = "Unlinked dog IDs %s from customer ID %d.";

    private final int customerId;
    private final List<Integer> dogIds;

    /** Creates an unlink request containing distinct nonnegative dog IDs and a nonnegative customer ID. */
    public UnlinkCommand(int customerId, List<Integer> dogIds) {
        this.dogIds = List.copyOf(dogIds);
        checkArgument(customerId >= 0 && !dogIds.isEmpty()
                && dogIds.stream().allMatch(id -> id >= 0)
                && new HashSet<>(dogIds).size() == dogIds.size(), "Invalid unlink IDs.");
        this.customerId = customerId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.unlinkDogs(customerId, dogIds);
        } catch (UnlinkException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds, customerId));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnlinkCommand otherCommand
                && customerId == otherCommand.customerId && dogIds.equals(otherCommand.dogIds);
    }
}
