package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/** Formats customer and dog card details as labelled plain text for the clipboard. */
final class CardCopyFormatter {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private CardCopyFormatter() {
    }

    /** Returns all displayed customer details in a labelled multiline format. */
    static String formatCustomer(Customer customer, List<Integer> ownedDogIds) {
        requireNonNull(customer);
        requireNonNull(ownedDogIds);
        String dogIds = ownedDogIds.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return String.join(LINE_SEPARATOR,
                "Customer ID: " + customer.getId(),
                "Name: " + customer.getName().fullName,
                "Phone: " + customer.getPhone().value,
                "Email: " + customer.getEmail().value,
                "Backup Contact: " + customer.getBackupContact().value,
                "Dog IDs: " + (dogIds.isEmpty() ? "None" : dogIds));
    }

    /** Returns all displayed dog details in a labelled multiline format. */
    static String formatDog(Dog dog) {
        requireNonNull(dog);
        return String.join(LINE_SEPARATOR,
                "Dog ID: " + dog.getId(),
                "Name: " + dog.getName().fullName,
                "Breed: " + dog.getBreed(),
                "Size: " + dog.getSize(),
                "Customer ID: " + dog.getOwnerId().map(String::valueOf).orElse("None"));
    }
}
