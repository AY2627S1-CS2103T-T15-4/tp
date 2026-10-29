package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.dog.DogSearchPredicate;

/** Searches dogs by name, breed, size, and ID. */
public class SearchDogCommand extends Command {
    public static final String COMMAND_WORD = "searchd";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [b/BREED] [s/SIZE] [i/ID]";
    public static final String MESSAGE_INVALID_ID = "Error in searching dogs:\n"
            + "Please enter a valid dog index";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully searched dogs by ";

    private final String name;
    private final String breed;
    private final String size;
    private final Integer id;
    private final DogSearchPredicate predicate;

    /**
     * Creates a dog filter with any combination of supported fields.
     *
     * @throws IllegalArgumentException if {@code id} is present but not positive
     */
    public SearchDogCommand(String name, String breed, String size, Integer id) {
        checkArgument(id == null || id >= 1, "Dog search ID must be positive.");
        this.name = name;
        this.breed = breed;
        this.size = size;
        this.id = id;
        this.predicate = null;
    }

    /** Creates a dog search from a prepared predicate. */
    public SearchDogCommand(DogSearchPredicate predicate) {
        this.name = null;
        this.breed = null;
        this.size = null;
        this.id = null;
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (id != null && model.getDogList().stream().noneMatch(dog -> dog.getId() == id)) {
            throw new CommandException(MESSAGE_INVALID_ID);
        }
        DogSearchPredicate dogPredicate = predicate == null
                ? new DogSearchPredicate(name, breed, size, id) : predicate;
        model.updateFilteredDogList(dogPredicate);
        model.updateFilteredCustomerList(customer -> model.getFilteredDogList().stream()
                .anyMatch(dog -> dog.getOwnerId()
                        .map(ownerId -> ownerId == customer.getId())
                        .orElse(false)));
        model.updateFilteredActivityList(activity -> model.getAddressBook().getEnrolmentList().stream()
                .filter(enrolment -> enrolment.getActivityId() == activity.getId())
                .anyMatch(enrolment -> model.getDogList().stream()
                        .filter(dog -> dog.getId() == enrolment.getDogId())
                        .anyMatch(dogPredicate)));
        model.setActivityEnrolmentDogPredicate(dogPredicate);
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
        return other == this || other instanceof SearchDogCommand otherCommand
                && Objects.equals(name, otherCommand.name)
                && Objects.equals(breed, otherCommand.breed)
                && Objects.equals(size, otherCommand.size)
                && Objects.equals(id, otherCommand.id);
    }
}
