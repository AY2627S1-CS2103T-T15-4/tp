//@@author its-shoutime
package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.customer.BackupContact;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.Email;
import seedu.address.model.customer.Name;
import seedu.address.model.customer.Phone;

/**
 * Jackson-friendly version of {@link Customer}.
 */
class JsonAdaptedCustomer {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Customer's %s field is missing!";

    private final Integer id;
    private final String name;
    private final String phone;
    private final String email;
    private final String backupContact;

    /**
     * Constructs a {@code JsonAdaptedCustomer} with the given customer details.
     */
    @JsonCreator
    public JsonAdaptedCustomer(@JsonProperty("id") Integer id, @JsonProperty("name") String name,
            @JsonProperty("phone") String phone, @JsonProperty("email") String email,
            @JsonProperty("backupContact") String backupContact) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.backupContact = backupContact;
    }

    /**
     * Converts a given {@code Customer} into this class for Jackson use.
     */
    public JsonAdaptedCustomer(Customer source) {
        requireNonNull(source);
        id = source.getId();
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        backupContact = source.getBackupContact().value;
    }

    /**
     * Converts this Jackson-friendly adapted customer object into the model's {@code Customer} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted customer.
     */
    public Customer toModelType() throws IllegalValueException {
        requirePresent(id, "id");
        requirePresent(name, "name");
        requirePresent(phone, "phone");
        requirePresent(email, "email");
        requirePresent(backupContact, "backupContact");
        if (id < 1) {
            throw new IllegalValueException("Customer ID must be positive.");
        }
        // @@author Kenneth-Chia
        try {
            return new Customer(id, new Name(name), new Phone(phone), new Email(email),
                    new BackupContact(backupContact));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage(), e);
        }
        // @@author
    }

    private static void requirePresent(Object value, String fieldName) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
    }

}
