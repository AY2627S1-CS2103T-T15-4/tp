// @@author XanderGoh
package seedu.address.model.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class CustomerSearchPredicateTest {
    private final Customer customer = new Customer(42, "Little Buddy", "91234567", "buddy@example.com", "98765432");

    @Test
    public void test_matchesOnlyChosenField() {
        assertTrue(new CustomerSearchPredicate(" BUD ").test(customer));
        assertTrue(new CustomerSearchPredicate("little bud").test(customer));
        assertFalse(new CustomerSearchPredicate("91234567").test(customer));
        assertFalse(new CustomerSearchPredicate("42").test(customer));
        assertTrue(new CustomerSearchPredicate(42).test(customer));
        assertFalse(new CustomerSearchPredicate(4).test(customer));
    }

    @Test
    public void test_matchesAllSupportedFieldsAndOwnedDogs() {
        Dog ownedDog = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small")).withOwner(42);
        CustomerSearchPredicate predicate = new CustomerSearchPredicate(
                "little   bud", "91234567", "BUDDY@EXAMPLE.COM", "98765432", Set.of(7), List.of(ownedDog));

        assertTrue(predicate.test(customer));
        assertFalse(new CustomerSearchPredicate("Little Buddy", "92345678", null, null,
                null, List.of()).test(customer));
        assertFalse(new CustomerSearchPredicate(null, null, null, null, Set.of(8), List.of(ownedDog))
                .test(customer));
    }

    @Test
    public void test_dogMatchingRequiresOwnership() {
        Dog sameIdWrongOwner = new Dog(7, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"))
                .withOwner(99);
        Dog differentIdOwnedByCustomer = new Dog(8, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"))
                .withOwner(42);

        assertFalse(new CustomerSearchPredicate(null, null, null, null, Set.of(7),
                List.of(sameIdWrongOwner)).test(customer));
        assertFalse(new CustomerSearchPredicate(null, null, null, null, Set.of(7),
                List.of(differentIdOwnedByCustomer)).test(customer));
    }

    @Test
    public void constructor_invalidQuery_throwsException() {
        assertThrows(NullPointerException.class, () -> new CustomerSearchPredicate(null));
        assertThrows(IllegalArgumentException.class, () -> new CustomerSearchPredicate("  "));
        assertThrows(IllegalArgumentException.class, () -> new CustomerSearchPredicate(-1));
        assertThrows(IllegalArgumentException.class, () -> new CustomerSearchPredicate(0));
    }

    @Test
    public void test_nullCustomer_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CustomerSearchPredicate("Buddy").test(null));
    }

    @Test
    public void equals_comparesSearchFieldAndValue() {
        CustomerSearchPredicate predicate = new CustomerSearchPredicate("Buddy");
        assertEquals(predicate, predicate);
        assertEquals(predicate, new CustomerSearchPredicate("Buddy"));
        assertEquals(predicate.hashCode(), new CustomerSearchPredicate("Buddy").hashCode());
        assertEquals(new CustomerSearchPredicate(42), new CustomerSearchPredicate(42));
        assertEquals(new CustomerSearchPredicate(42).hashCode(), new CustomerSearchPredicate(42).hashCode());
        assertNotEquals(predicate, new CustomerSearchPredicate("Max"));
        assertFalse(new CustomerSearchPredicate("42").equals(new CustomerSearchPredicate(42)));
        assertFalse(new CustomerSearchPredicate(42).equals(new CustomerSearchPredicate(4)));
        assertFalse(new CustomerSearchPredicate(42).equals(null));
        assertNotEquals(predicate, new Object());
    }
}
