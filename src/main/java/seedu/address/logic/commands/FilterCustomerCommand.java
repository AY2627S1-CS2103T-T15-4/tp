package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.CustomerFilterPredicate;

/** Filters customers by name, contact details, and owned dogs. */
public class FilterCustomerCommand extends Command {
    public static final String COMMAND_WORD = "filterc";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_IDS] [bc/BACKUP_CONTACT]";
    public static final String MESSAGE_INVALID_DOG_ID = "Error in filtering customers:\n"
            + "Invalid dog id. Please input a valid dog id.";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully filtered customers by ";

    private final String name;
    private final String phone;
    private final String email;
    private final String backupContact;
    private final Set<Integer> dogIds;

    /** Creates a customer filter with any combination of supported fields. */
    public FilterCustomerCommand(String name, String phone, String email,
                                 String backupContact, Set<Integer> dogIds) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
        this.dogIds = dogIds;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (dogIds != null && dogIds.stream()
                .anyMatch(id -> model.getDogList().stream().noneMatch(dog -> dog.getId() == id))) {
            throw new CommandException(MESSAGE_INVALID_DOG_ID);
        }
        model.updateFilteredCustomerList(new CustomerFilterPredicate(name, phone, email, backupContact,
                dogIds, model.getDogList()));
        Set<Integer> matchingCustomerIds = model.getFilteredCustomerList().stream()
                .map(customer -> customer.getId())
                .collect(Collectors.toSet());
        model.updateFilteredDogList(dog -> dog.getOwnerId()
                .map(matchingCustomerIds::contains)
                .orElse(false));
        return new CommandResult(createSuccessMessage());
    }

    private String createSuccessMessage() {
        StringJoiner fields = new StringJoiner(", ");
        if (name != null) {
            fields.add("name: " + name);
        }
        if (phone != null) {
            fields.add("phone: " + phone);
        }
        if (email != null) {
            fields.add("email: " + email);
        }
        if (dogIds != null) {
            fields.add("dog ids: " + dogIds.stream().sorted(Comparator.naturalOrder())
                    .map(String::valueOf).collect(Collectors.joining(" ")));
        }
        if (backupContact != null) {
            fields.add("backup contact: " + backupContact);
        }
        return MESSAGE_SUCCESS_PREFIX + fields;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FilterCustomerCommand otherCommand)) {
            return false;
        }
        return Objects.equals(name, otherCommand.name)
                && Objects.equals(phone, otherCommand.phone)
                && Objects.equals(email, otherCommand.email)
                && Objects.equals(backupContact, otherCommand.backupContact)
                && Objects.equals(dogIds, otherCommand.dogIds);
    }
}
