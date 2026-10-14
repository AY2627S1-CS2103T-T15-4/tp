package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void constructor_invalidQuery_throwsException() {
        assertThrows(NullPointerException.class, () -> new DogSearchPredicate(null));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate("  "));
        assertThrows(IllegalArgumentException.class, () -> new DogSearchPredicate(-1));
    }

    @Test
    public void equals_comparesSearchFieldAndValue() {
        assertEquals(new DogSearchPredicate("Buddy"), new DogSearchPredicate("Buddy"));
        assertEquals(new DogSearchPredicate(42), new DogSearchPredicate(42));
        assertFalse(new DogSearchPredicate("42").equals(new DogSearchPredicate(42)));
        assertFalse(new DogSearchPredicate(42).equals(new DogSearchPredicate(4)));
        assertFalse(new DogSearchPredicate(42).equals(null));
    }
}
