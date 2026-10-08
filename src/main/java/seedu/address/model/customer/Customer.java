package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a customer in the daycare database.
 * Guarantees: details are present and not null, id is nonnegative, immutable.
 */
public class Customer {

    private final int id;
    private final String name;
    private final String phone;
    private final String email;
    private final String backupContact;

    /**
     * Every field must be present and not null. {@code id} must be nonnegative.
     * Name, phone, email, and backup contact are stored as given, including internal spaces and letter case.
     */
    public Customer(int id, String name, String phone, String email, String backupContact) {
        requireAllNonNull(name, phone, email, backupContact);
        checkArgument(id >= 0, "Customer id must be nonnegative.");
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getBackupContact() {
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

        return otherCustomer != null && otherCustomer.getId() == getId();
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
