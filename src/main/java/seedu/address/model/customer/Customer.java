package seedu.address.model.customer;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a customer in the daycare database.
 * Guarantees: details are present and not null, and immutable. Stored customers have nonnegative IDs.
 */
public class Customer {

    private final int id;
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final BackupContact backupContact;

    /**
     * Every field must be present and not null. {@code id} must be nonnegative.
     * Name, phone, email, and backup contact are stored as given, including internal spaces and letter case.
     */
    public Customer(int id, Name name, Phone phone, Email email, BackupContact backupContact) {
        requireAllNonNull(name, phone, email, backupContact);
        checkArgument(id >= 0, "Customer id must be nonnegative.");
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
    }

    /**
     * Creates a customer without an assigned persistent ID.
     * The address book assigns an ID before storing the customer.
     */
    public Customer(Name name, Phone phone, Email email, BackupContact backupContact) {
        requireAllNonNull(name, phone, email, backupContact);
        this.id = -1;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
    }

    /**
     * Constructs a customer from raw field values.
     *
     * @param id A nonnegative customer identifier.
     * @param name A valid customer name.
     * @param phone A valid primary phone number.
     * @param email A valid email address.
     * @param backupContact A valid backup phone number.
     */
    public Customer(int id, String name, String phone, String email, String backupContact) {
        this(id, new Name(name), new Phone(phone), new Email(email), new BackupContact(backupContact));
    }

    public int getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public BackupContact getBackupContact() {
        return backupContact;
    }

    /**
     * Returns true if both customers have the same id.
     * This defines a weaker notion of equality between two customers.
     */
    public boolean isSameCustomer(Customer otherCustomer) {
        if (otherCustomer == this) {
            return true;
        }

        return otherCustomer != null && id >= 0 && otherCustomer.getId() == id;
    }

    /**
     * Returns true if both customers have the same identity and data fields.
     * This defines a stronger notion of equality between two customers.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Customer otherCustomer)) {
            return false;
        }

        return id == otherCustomer.id
                && name.equals(otherCustomer.name)
                && phone.equals(otherCustomer.phone)
                && email.equals(otherCustomer.email)
                && backupContact.equals(otherCustomer.backupContact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone, email, backupContact);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("backupContact", backupContact)
                .toString();
    }

}
