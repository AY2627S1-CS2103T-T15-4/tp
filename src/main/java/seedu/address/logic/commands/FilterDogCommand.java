package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.DogFilterPredicate;

/** Filters dogs by name, breed, size, and ID. */
public class FilterDogCommand extends Command {
    public static final String COMMAND_WORD = "filterd";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [b/BREED] [s/SIZE] [i/ID]";
    public static final String MESSAGE_INVALID_ID = "Error in filtering dogs:\n"
            + "Info is in the wrong format. Input in the correct format";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully filtered dogs by ";

    private final String name;
    private final String breed;
    private final String size;
    private final Integer id;

    /** Creates a dog filter with any combination of supported fields. */
    public FilterDogCommand(String name, String breed, String size, Integer id) {
        this.name = name;
        this.breed = breed;
        this.size = size;
        this.id = id;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (id != null && model.getDogList().stream().noneMatch(dog -> dog.getId() == id)) {
            throw new CommandException("Error in filtering dogs:\n"
                    + "Invalid dog id. Please input a valid dog id.");
        }
        model.updateFilteredDogList(new DogFilterPredicate(name, breed, size, id));
        model.updateFilteredCustomerList(customer -> model.getFilteredDogList().stream()
                .anyMatch(dog -> dog.getOwnerId()
                        .map(ownerId -> ownerId == customer.getId())
                        .orElse(false)));
        return new CommandResult(createSuccessMessage());
    }

    private String createSuccessMessage() {
        StringBuilder fields = new StringBuilder(MESSAGE_SUCCESS_PREFIX);
        appendField(fields, "name", name);
        appendField(fields, "breed", breed);
        appendField(fields, "size", size);
        appendField(fields, "id", id);
        return fields.toString();
    }

    private static void appendField(StringBuilder fields, String label, Object value) {
        if (value != null) {
            if (fields.length() > MESSAGE_SUCCESS_PREFIX.length()) {
                fields.append(", ");
            }
            fields.append(label).append(": ").append(value);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FilterDogCommand otherCommand
                && Objects.equals(name, otherCommand.name)
                && Objects.equals(breed, otherCommand.breed)
                && Objects.equals(size, otherCommand.size)
                && Objects.equals(id, otherCommand.id);
    }
}
