package seedu.address.model.customer;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.customer.*;
import seedu.address.model.customer.Address;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

public class Customer {

    // Identity fields
    private final seedu.address.model.customer.Name name;
    private final seedu.address.model.customer.Phone phone;
    private final seedu.address.model.customer.Email email;

    // Data fields
    private final seedu.address.model.customer.Address address;

    /**
     * Every field must be present and not null.
     */
    public Customer(seedu.address.model.customer.Name name, seedu.address.model.customer.Phone phone, seedu.address.model.customer.Email email, seedu.address.model.customer.Address address) {
        requireAllNonNull(name, phone, email, address);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
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

    public Address getAddress() {
        return address;
    }


    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSameCustomer(Customer otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Customer otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .toString();
    }
}
