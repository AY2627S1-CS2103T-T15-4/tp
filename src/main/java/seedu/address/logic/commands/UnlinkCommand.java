// @@author Kenneth-Chia
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.exceptions.UnlinkException;

/** Unlinks selected dogs or all dogs belonging to a customer using persistent IDs. */
public class UnlinkCommand extends Command {
    public static final String COMMAND_WORD = "unlink";
    public static final String MESSAGE_USAGE = "unlink: Unlinks selected dogs or all dogs owned by a customer.\n"
            + "Parameters: d/DOG_ID [DOG_ID]... OR c/CUSTOMER_ID\n"
            + "Customer and dog IDs must be positive; dog IDs must not repeat.\n"

            + "The entire batch is rejected if any selected dog is missing or has no owner.\n"
            + "The customer form requires an existing customer with linked dogs; do not mix prefixes.\n"
            + "Examples: unlink d/2 3 | unlink c/1";
    public static final String MESSAGE_SUCCESS = "Unlinked dog IDs %s from their owners.";

    public static final String MESSAGE_CUSTOMER_SUCCESS = "Unlinked all dogs from customer ID %d.";

    private final Integer customerId;
    private final List<Integer> dogIds;

    /** Creates an unlink request containing distinct positive dog IDs. */
    public UnlinkCommand(List<Integer> dogIds) {
        this.dogIds = List.copyOf(dogIds);
        checkArgument(!this.dogIds.isEmpty()
                && this.dogIds.stream().allMatch(id -> id >= 1)
                && new HashSet<>(this.dogIds).size() == this.dogIds.size(), "Invalid unlink IDs.");
        this.customerId = null;
    }

    /** Creates a request to unlink all dogs belonging to a positive customer ID. */
    public UnlinkCommand(int customerId) {
        checkArgument(customerId > 0, "Invalid customer ID.");
        this.customerId = customerId;
        this.dogIds = List.of();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            if (customerId != null) {
                model.unlinkCustomerDogs(customerId);
                return new CommandResult(String.format(MESSAGE_CUSTOMER_SUCCESS, customerId));
            }
            model.unlinkDogs(dogIds);
        } catch (UnlinkException e) {
            throw new CommandException(e.getMessage(), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, dogIds));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UnlinkCommand otherCommand
                && Objects.equals(customerId, otherCommand.customerId) && dogIds.equals(otherCommand.dogIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, dogIds);
    }
}
// @@author
