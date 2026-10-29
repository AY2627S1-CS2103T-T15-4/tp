package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DogOwnershipTest {
    private final Dog unlinked = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));

    @Test
    public void isSameDog_unassignedDogs_doNotShareIdentity() {
        Dog first = new Dog(unlinked.getName(), unlinked.getBreed(), unlinked.getSize());
        Dog second = new Dog(unlinked.getName(), unlinked.getBreed(), unlinked.getSize());
        assertTrue(first.isSameDog(first));
        assertFalse(first.isSameDog(second));
        assertFalse(second.isSameDog(first));
        assertFalse(first.isSameDog(unlinked));
        assertFalse(unlinked.isSameDog(first));
        assertFalse(unlinked.isSameDog(null));
    }

    @Test
    public void constructor_idBoundariesAndNullDetails_rejectedIndependently() {
        for (int id : new int[] {Integer.MIN_VALUE, -1, 0}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new Dog(id, unlinked.getName(), unlinked.getBreed(), unlinked.getSize()));
        }
        Dog maximum = new Dog(Integer.MAX_VALUE, unlinked.getName(), unlinked.getBreed(), unlinked.getSize());
        assertEquals(Integer.MAX_VALUE, maximum.withOwner(Integer.MAX_VALUE).getOwnerId().orElseThrow());
        assertThrows(NullPointerException.class, () ->
                new Dog(1, null, unlinked.getBreed(), unlinked.getSize()));
        assertThrows(NullPointerException.class, () ->
                new Dog(1, unlinked.getName(), null, unlinked.getSize()));
        assertThrows(NullPointerException.class, () ->
                new Dog(1, unlinked.getName(), unlinked.getBreed(), null));
    }

    @Test
    public void withoutOwner_preservesDetailsAndOriginal() {
        Dog linked = unlinked.withOwner(7);
        assertEquals(unlinked, linked.withoutOwner());
        assertEquals(Optional.of(7), linked.getOwnerId());
        assertEquals(unlinked, unlinked.withoutOwner());
    }

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
    public void withOwner_nonPositiveId_rejected() {
        assertThrows(IllegalArgumentException.class, () -> unlinked.withOwner(0));
        assertThrows(IllegalArgumentException.class, () -> unlinked.withOwner(-1));
    }
}
