// @@author XanderGoh
package seedu.address.model.customer;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.model.dog.Dog;

/** Filters customers by their details and the dogs they own. */
public class CustomerSearchPredicate implements Predicate<Customer> {
    private final String nameQuery;
    private final String phone;
    private final String email;
    private final String backupContact;
    private final Set<Integer> dogIds;
    private final List<Dog> dogs;
    private final Integer customerId;

    /** Creates a predicate that searches customer names by substring. */
    public CustomerSearchPredicate(String nameQuery) {
        this(validateName(nameQuery), null, null, null, null, List.of(), null);
    }

    /** Creates a predicate that searches for one customer ID. */
    public CustomerSearchPredicate(int customerId) {
        this(null, null, null, null, null, List.of(), validateId(customerId));
    }

    /** Creates a predicate using the supplied customer fields and stored dogs. */
    public CustomerSearchPredicate(String nameQuery, String phone, String email,
                                   String backupContact, Set<Integer> dogIds, List<Dog> dogs) {
        this(nameQuery, phone, email, backupContact, dogIds, dogs, null);
    }

    private CustomerSearchPredicate(String nameQuery, String phone, String email,
                                    String backupContact, Set<Integer> dogIds, List<Dog> dogs,
                                    Integer customerId) {
        if (dogIds != null && dogIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw new IllegalArgumentException("Dog search IDs must be positive.");
        }
        this.nameQuery = nameQuery == null ? null : normalize(nameQuery);
        this.phone = phone;
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
        this.backupContact = backupContact;
        this.dogIds = dogIds == null ? null : Set.copyOf(dogIds);
        this.dogs = requireNonNull(dogs);
        this.customerId = customerId;
    }

    @Override
    public boolean test(Customer customer) {
        requireNonNull(customer);
        return (customerId == null || customer.getId() == customerId)
                && matchesName(customer)
                && matchesPhone(customer)
                && matchesEmail(customer)
                && matchesBackupContact(customer)
                && matchesDogs(customer);
    }

    private boolean matchesName(Customer customer) {
        return nameQuery == null || normalize(customer.getName().fullName).contains(nameQuery);
    }

    private boolean matchesPhone(Customer customer) {
        return phone == null || customer.getPhone().value.equals(phone);
    }

    private boolean matchesEmail(Customer customer) {
        return email == null || customer.getEmail().value.toLowerCase(Locale.ROOT).equals(email);
    }

    private boolean matchesBackupContact(Customer customer) {
        return backupContact == null || customer.getBackupContact().value.equals(backupContact);
    }

    private boolean matchesDogs(Customer customer) {
        if (dogIds == null) {
            return true;
        }
        return dogIds.stream().allMatch(dogId -> dogs.stream()
                .anyMatch(dog -> dog.getId() == dogId
                        && dog.getOwnerId().orElse(-1) == customer.getId()));
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String validateName(String value) {
        Objects.requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Customer search query cannot be blank");
        }
        return value;
    }

    private static int validateId(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Customer search ID must be positive");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CustomerSearchPredicate otherPredicate)) {
            return false;
        }
        return Objects.equals(nameQuery, otherPredicate.nameQuery)
                && Objects.equals(phone, otherPredicate.phone)
                && Objects.equals(email, otherPredicate.email)
                && Objects.equals(backupContact, otherPredicate.backupContact)
                && Objects.equals(dogIds, otherPredicate.dogIds)
                && Objects.equals(customerId, otherPredicate.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameQuery, phone, email, backupContact, dogIds, customerId);
    }
}
