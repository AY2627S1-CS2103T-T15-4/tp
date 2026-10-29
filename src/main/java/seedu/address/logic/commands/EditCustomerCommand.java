package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BACKUP_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

/**
 * Edits the details of an existing customer in WatchDog.
 */
public class EditCustomerCommand extends Command {

    public static final String COMMAND_WORD = "editcustomer";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits a customer identified by ID. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: CUSTOMER_ID (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_BACKUP_CONTACT + "BACKUP_CONTACT]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_CUSTOMER_SUCCESS = "Edited customer: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_CUSTOMER_NOT_FOUND = "This customer does not exist.";

    private final int customerId;
    private final EditCustomerDescriptor editCustomerDescriptor;

    /**
     * Creates a command to edit the customer with the specified ID and replacement details.
     *
     * @param customerId persistent ID of the customer to edit.
     * @param editCustomerDescriptor details to edit the customer with.
     */
    public EditCustomerCommand(int customerId, EditCustomerDescriptor editCustomerDescriptor) {
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        requireNonNull(editCustomerDescriptor);

        this.customerId = customerId;
        this.editCustomerDescriptor = new EditCustomerDescriptor(editCustomerDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Customer customerToEdit = model.getWatchDog().getCustomerList().stream()
                .filter(customer -> customer.getId() == customerId)
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_CUSTOMER_NOT_FOUND));
        Customer editedCustomer = createEditedCustomer(customerToEdit, editCustomerDescriptor);

        CustomerContactValidator.validate(editedCustomer, model.getCustomerList(), customerId);
        model.setCustomer(customerToEdit, editedCustomer);
        return new CommandResult(String.format(MESSAGE_EDIT_CUSTOMER_SUCCESS, Messages.format(editedCustomer)));
    }

    /**
     * Creates and returns a customer with the details of {@code customerToEdit} updated by the descriptor.
     */
    private static Customer createEditedCustomer(Customer customerToEdit,
            EditCustomerDescriptor editCustomerDescriptor) {
        assert customerToEdit != null;

        Name updatedName = editCustomerDescriptor.getName().orElse(customerToEdit.getName());
        Phone updatedPhone = editCustomerDescriptor.getPhone().orElse(customerToEdit.getPhone());
        Email updatedEmail = editCustomerDescriptor.getEmail().orElse(customerToEdit.getEmail());
        BackupContact updatedBackupContact = editCustomerDescriptor.getBackupContact()
                .orElse(customerToEdit.getBackupContact());

        return new Customer(customerToEdit.getId(), updatedName, updatedPhone, updatedEmail, updatedBackupContact);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCustomerCommand otherEditCommand)) {
            return false;
        }

        return customerId == otherEditCommand.customerId
                && editCustomerDescriptor.equals(otherEditCommand.editCustomerDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("customerId", customerId)
                .add("editCustomerDescriptor", editCustomerDescriptor)
                .toString();
    }

    /**
     * Stores the fields to edit. Each non-null value replaces the corresponding customer field.
     */
    public static class EditCustomerDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private BackupContact backupContact;

        public EditCustomerDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of each supplied field is used internally.
         */
        public EditCustomerDescriptor(EditCustomerDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setBackupContact(toCopy.backupContact);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, backupContact);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setBackupContact(BackupContact backupContact) {
            this.backupContact = backupContact;
        }

        public Optional<BackupContact> getBackupContact() {
            return Optional.ofNullable(backupContact);
        }


        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditCustomerDescriptor otherEditCustomerDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditCustomerDescriptor.name)
                    && Objects.equals(phone, otherEditCustomerDescriptor.phone)
                    && Objects.equals(email, otherEditCustomerDescriptor.email)
                    && Objects.equals(backupContact, otherEditCustomerDescriptor.backupContact);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, phone, email, backupContact);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("backupContact", backupContact)
                    .toString();
        }
    }
}
