package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.CustomerSearchPredicate;

/** Searches customers by name, contact details, owned dogs, and customer ID. */
public class SearchCustomerCommand extends Command {
    public static final String COMMAND_WORD = "searchc";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_IDS] [bc/BACKUP_CONTACT] [i/CUSTOMER_ID]";
    public static final String MESSAGE_INVALID_DOG_ID = "Error in searching customers:\n"
            + "Invalid dog id. Please input a valid dog id.";
    public static final String MESSAGE_INVALID_CUSTOMER_ID = "Error in searching customers:\n"
            + "Invalid customer id. Please input a valid customer id.";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully searched customers by ";

    private final String name;
    private final String phone;
    private final String email;
    private final String backupContact;
    private final Set<Integer> dogIds;
    private final Integer customerId;
    private final CustomerSearchPredicate predicate;

    /** Creates a customer search with any combination of supported fields except customer ID. */
    public SearchCustomerCommand(String name, String phone, String email,
                                 String backupContact, Set<Integer> dogIds) {
        this(name, phone, email, backupContact, dogIds, null);
    }

    /** Creates a customer search with any combination of supported fields. */
    public SearchCustomerCommand(String name, String phone, String email,
                                 String backupContact, Set<Integer> dogIds, Integer customerId) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
        this.dogIds = dogIds;
        this.customerId = customerId;
        this.predicate = null;
    }

    /** Creates a customer search from a prepared predicate. */
    public SearchCustomerCommand(CustomerSearchPredicate predicate) {
        this.name = null;
        this.phone = null;
        this.email = null;
        this.backupContact = null;
        this.dogIds = null;
        this.customerId = null;
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (customerId != null && model.getCustomerList().stream()
                .noneMatch(customer -> customer.getId() == customerId)) {
            throw new CommandException(MESSAGE_INVALID_CUSTOMER_ID);
        }
        if (dogIds != null && dogIds.stream()
                .anyMatch(id -> model.getDogList().stream().noneMatch(dog -> dog.getId() == id))) {
            throw new CommandException(MESSAGE_INVALID_DOG_ID);
        }
        CustomerSearchPredicate customerPredicate = predicate == null
                ? new CustomerSearchPredicate(name, phone, email, backupContact, dogIds, model.getDogList())
                : predicate;
        model.updateFilteredCustomerList(customer -> (customerId == null || customer.getId() == customerId)
                && customerPredicate.test(customer));
        model.updateFilteredDogList(dog -> dog.getOwnerId()
                .map(ownerId -> model.getFilteredCustomerList().stream()
                        .anyMatch(customer -> customer.getId() == ownerId))
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
        if (customerId != null) {
            fields.add("customer id: " + customerId);
        }
        return MESSAGE_SUCCESS_PREFIX + fields;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SearchCustomerCommand otherCommand)) {
            return false;
        }
        return Objects.equals(name, otherCommand.name)
                && Objects.equals(phone, otherCommand.phone)
                && Objects.equals(email, otherCommand.email)
                && Objects.equals(backupContact, otherCommand.backupContact)
                && Objects.equals(dogIds, otherCommand.dogIds)
                && Objects.equals(customerId, otherCommand.customerId);
    }
}
