package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DogOwnershipTest {
    private final Dog unlinked = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));

    @Test
    public void withOwner_preservesIdentityAndDetailsWithoutMutatingOriginal() {
        Dog linked = unlinked.withOwner(7);
        assertTrue(unlinked.getOwnerId().isEmpty());
        assertEquals(Optional.of(7), linked.getOwnerId());
        assertTrue(unlinked.isSameDog(linked));
        assertEquals(unlinked.getName(), linked.getName());
        assertEquals(unlinked.getBreed(), linked.getBreed());
        assertEquals(unlinked.getSize(), linked.getSize());
        assertNotEquals(unlinked, linked);
        assertNotEquals(linked, unlinked.withOwner(8));
        assertEquals(linked, unlinked.withOwner(7));
        assertEquals(linked.hashCode(), unlinked.withOwner(7).hashCode());
    }

    @Test
    public void withOwner_negativeId_rejected() {
        assertThrows(IllegalArgumentException.class, () -> unlinked.withOwner(-1));
    }
}
