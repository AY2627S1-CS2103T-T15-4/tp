package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/** Adds, changes, or clears a person's remark. */
public class RemarkCommand extends Command {
    public static final String COMMAND_WORD = "remark";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the remark of the person at INDEX.\n"
            + "Parameters: INDEX (positive integer) " + PREFIX_REMARK + "[REMARK]\n"
            + "Example: remark 1 r/Likes to swim";
    public static final String MESSAGE_SUCCESS = "Updated remark for person: %s";

    private final Index index;
    private final Remark remark;

    public RemarkCommand(Index index, Remark remark) {
        requireNonNull(index);
        requireNonNull(remark);
        this.index = index;
        this.remark = remark;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> shown = model.getFilteredPersonList();
        if (index.getZeroBased() >= shown.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person original = shown.get(index.getZeroBased());
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getAddress(), remark, original.getTags());
        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updated)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RemarkCommand command
                && index.equals(command.index) && remark.equals(command.remark);
    }
}
