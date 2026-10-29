package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class CardCopyFormatterTest {

    private static final String NEW_LINE = System.lineSeparator();

    @Test
    public void formatCustomer_returnsLabelledDetailsWithSortedDogIds() {
        Customer customer = new Customer(7, "Alex Tan", "91234567", "alex@example.com", "98765432");

        assertEquals(String.join(NEW_LINE,
                "Customer ID: 7",
                "Name: Alex Tan",
                "Phone: 91234567",
                "Email: alex@example.com",
                "Backup Contact: 98765432",
                "Dog IDs: 2, 4"), CardCopyFormatter.formatCustomer(customer, List.of(4, 2)));
        assertEquals(String.join(NEW_LINE,
                "Customer ID: 7",
                "Name: Alex Tan",
                "Phone: 91234567",
                "Email: alex@example.com",
                "Backup Contact: 98765432",
                "Dog IDs: None"), CardCopyFormatter.formatCustomer(customer, List.of()));
    }

    @Test
    public void formatDog_returnsLabelledDetailsForLinkedAndUnlinkedDogs() {
        Dog dog = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));

        assertEquals(String.join(NEW_LINE,
                "Dog ID: 42",
                "Name: Buddy",
                "Breed: Poodle",
                "Size: small",
                "Customer ID: None"), CardCopyFormatter.formatDog(dog));
        assertEquals(String.join(NEW_LINE,
                "Dog ID: 42",
                "Name: Buddy",
                "Breed: Poodle",
                "Size: small",
                "Customer ID: 7"), CardCopyFormatter.formatDog(dog.withOwner(7)));
    }
}
