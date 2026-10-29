package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DogSearchPredicateTest {
    private final Dog dog = new Dog(42, new DogName("Little Buddy"), new Breed("Poodle"), new Size("small"));

    @Test
    public void test_matchesOnlyChosenField() {
        assertTrue(new DogSearchPredicate(" BUD ").test(dog));
        assertTrue(new DogSearchPredicate("little bud").test(dog));
        assertFalse(new DogSearchPredicate("Poodle").test(dog));
        assertFalse(new DogSearchPredicate("42").test(dog));
        assertTrue(new DogSearchPredicate(42).test(dog));
        assertFalse(new DogSearchPredicate(4).test(dog));
    }

    @Test
    public void test_matchesAllSupportedFields() {
        assertTrue(new DogSearchPredicate("little buddy", "poodle", "SMALL", 42).test(dog));
        assertFalse(new DogSearchPredicate("little buddy", "labrador", "small", 42).test(dog));
        assertFalse(new DogSearchPredicate("little buddy", "poodle", "large", 42).test(dog));
        assertFalse(new DogSearchPredicate("little buddy", "poodle", "small", 7).test(dog));
    }

    @Test
    public void constructor_invalidQuery_throwsException() {
        assertThrows(NullPointerException.class, () -> new DogSearchPredicate(null));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate("  "));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate(-1));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate(0));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate(null, null, null, 0));
    }

    @Test
    public void test_nullDog_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DogSearchPredicate("Buddy").test(null));
    }

    @Test
    public void equals_comparesSearchFieldAndValue() {
        DogSearchPredicate predicate = new DogSearchPredicate("Buddy");
        assertEquals(predicate, predicate);
        assertEquals(predicate, new DogSearchPredicate("Buddy"));
        assertEquals(predicate.hashCode(), new DogSearchPredicate("Buddy").hashCode());
        assertEquals(new DogSearchPredicate(42), new DogSearchPredicate(42));
        assertEquals(new DogSearchPredicate(42).hashCode(), new DogSearchPredicate(42).hashCode());
        assertNotEquals(predicate, new DogSearchPredicate("Max"));
        assertFalse(new DogSearchPredicate("42").equals(new DogSearchPredicate(42)));
        assertFalse(new DogSearchPredicate(42).equals(new DogSearchPredicate(4)));
        assertFalse(new DogSearchPredicate(42).equals(null));
        assertNotEquals(predicate, new Object());
    }
}
