package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.model.dog.Dog;

/** Searches customers by name, contact details, owned dogs, and customer ID. */
public class SearchCustomerCommand extends Command {
    public static final String COMMAND_WORD = "searchc";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [p/PHONE] [e/EMAIL] [d/DOG_IDS] [bc/BACKUP_CONTACT] [i/CUSTOMER_ID]";
    public static final String MESSAGE_INVALID_DOG_ID = "Error in searching customers:\n"
            + "Please enter a valid dog ID";
    public static final String MESSAGE_INVALID_CUSTOMER_ID = "Error in searching customers:\n"
            + "Please enter a valid customer ID";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully searched customers by ";

    private final String name;
    private final String phone;
    private final String email;
    private final String backupContact;
    private final Set<Integer> dogIds;
    private final Integer customerId;
    private final CustomerSearchPredicate predicate;

    /** Creates a customer search using the supplied fields and, when present, positive dog IDs. */
    public SearchCustomerCommand(String name, String phone, String email,
                                 String backupContact, Set<Integer> dogIds) {
        this(name, phone, email, backupContact, dogIds, null);
    }

    /**
     * Creates a customer search using the supplied fields and, when present, positive dog IDs.
     *
     * @throws IllegalArgumentException if any supplied dog or customer ID is not positive
     */
    public SearchCustomerCommand(String name, String phone, String email,
                                 String backupContact, Set<Integer> dogIds, Integer customerId) {
        checkArgument(dogIds == null || dogIds.stream().allMatch(id -> id != null && id >= 1),
                "Dog search IDs must be positive.");
        checkArgument(customerId == null || customerId >= 1, "Customer search ID must be positive.");
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
        this.dogIds = dogIds == null ? null : Set.copyOf(dogIds);
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
        Predicate<Customer> matchingCustomer = customer -> customerPredicate.test(customer)
                && (customerId == null || customer.getId() == customerId);
        model.updateFilteredCustomerList(matchingCustomer);
        model.updateFilteredDogList(dog -> ownsMatchingCustomer(model, dog, matchingCustomer));
        model.updateFilteredActivityList(activity -> hasMatchingEnrolment(model, activity, matchingCustomer));
        model.setActivityEnrolmentDogPredicate(dog -> ownsMatchingCustomer(model, dog, matchingCustomer));
        return new CommandResult(createSuccessMessage());
    }

    private static boolean ownsMatchingCustomer(Model model, Dog dog, Predicate<Customer> matchingCustomer) {
        return dog.getOwnerId().map(ownerId -> model.getCustomerList().stream()
                .anyMatch(customer -> customer.getId() == ownerId && matchingCustomer.test(customer))).orElse(false);
    }

    private static boolean hasMatchingEnrolment(Model model, Activity activity,
                                                Predicate<Customer> matchingCustomer) {
        return model.getAddressBook().getEnrolmentList().stream()
                .filter(enrolment -> enrolment.getActivityId() == activity.getId())
                .anyMatch(enrolment -> model.getDogList().stream()
                        .filter(dog -> dog.getId() == enrolment.getDogId())
                        .anyMatch(dog -> ownsMatchingCustomer(model, dog, matchingCustomer)));
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
