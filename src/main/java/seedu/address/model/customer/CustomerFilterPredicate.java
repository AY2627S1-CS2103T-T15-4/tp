package seedu.address.model.customer;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.model.dog.Dog;

/** Filters customers by their details and the dogs they own. */
public class CustomerFilterPredicate implements Predicate<Customer> {
    private final String nameQuery;
    private final String phone;
    private final String email;
    private final String backupContact;
    private final Set<Integer> dogIds;
    private final List<Dog> dogs;

    /** Creates a predicate using the supplied customer fields and stored dogs. */
    public CustomerFilterPredicate(String nameQuery, String phone, String email,
                                   String backupContact, Set<Integer> dogIds, List<Dog> dogs) {
        this.nameQuery = nameQuery == null ? null : normalize(nameQuery);
        this.phone = phone;
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
        this.backupContact = backupContact;
        this.dogIds = dogIds;
        this.dogs = dogs;
    }

    @Override
    public boolean test(Customer customer) {
        return matchesName(customer)
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
}
