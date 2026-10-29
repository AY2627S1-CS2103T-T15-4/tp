package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.dog.exceptions.DuplicateDogException;

public class UniqueDogListTest {
    private final UniqueDogList uniqueDogList = new UniqueDogList();

    @Test
    public void add_duplicateId_throwsDuplicateDogException() {
        Dog firstDog = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog secondDog = new Dog(1, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));
        uniqueDogList.add(firstDog);

        assertThrows(DuplicateDogException.class, () -> uniqueDogList.add(secondDog));
    }

    @Test
    public void add_unassignedDog_rejectsDogWithoutPersistentId() {
        Dog unassigned = new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));

        assertThrows(IllegalArgumentException.class, "Dog ID must be positive.", () ->
                uniqueDogList.add(unassigned));
    }

    @Test
    public void setDogs_duplicateId_throwsDuplicateDogException() {
        Dog firstDog = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog secondDog = new Dog(1, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));

        assertThrows(DuplicateDogException.class, () -> uniqueDogList.setDogs(List.of(firstDog, secondDog)));
    }

    @Test
    public void setDog_validDog_replacesTarget() {
        Dog original = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog edited = new Dog(1, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));
        uniqueDogList.add(original);

        uniqueDogList.setDog(original, edited);

        assertEquals(List.of(edited), uniqueDogList.asUnmodifiableObservableList());
    }

    @Test
    public void setDog_missingTarget_throwsException() {
        Dog target = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog edited = new Dog(1, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));
        uniqueDogList.add(new Dog(2, new DogName("Other"), new Breed("Beagle"), new Size("Large")));
        assertThrows(IllegalArgumentException.class, () -> uniqueDogList.setDog(target, edited));
    }

    @Test
    public void setDog_changedId_throwsException() {
        Dog original = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog edited = new Dog(2, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));
        uniqueDogList.add(original);
        assertThrows(IllegalArgumentException.class, () -> uniqueDogList.setDog(original, edited));
    }

    @Test
    public void utilityMethods_returnExpectedValues() {
        UniqueDogList anotherList = new UniqueDogList();
        Dog dog = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        uniqueDogList.add(dog);
        anotherList.add(dog);

        assertTrue(uniqueDogList.equals(uniqueDogList));
        assertTrue(uniqueDogList.equals(anotherList));
        assertFalse(uniqueDogList.equals(null));
        assertFalse(uniqueDogList.equals("dogs"));
        assertEquals(uniqueDogList.hashCode(), anotherList.hashCode());
        assertTrue(uniqueDogList.toString().contains("Cookie"));
        assertEquals(1, uniqueDogList.iterator().next().getId());
    }

}
