package seedu.address.model.dog;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void add_unassignedDogs_allowsBothDogs() {
        uniqueDogList.add(new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("Small")));
        uniqueDogList.add(new Dog(new DogName("Cookie"), new Breed("Poodle"), new Size("Small")));

        assertEquals(2, uniqueDogList.asUnmodifiableObservableList().size());
    }

    @Test
    public void setDogs_duplicateId_throwsDuplicateDogException() {
        Dog firstDog = new Dog(1, new DogName("Cookie"), new Breed("Poodle"), new Size("Small"));
        Dog secondDog = new Dog(1, new DogName("Milo"), new Breed("Beagle"), new Size("Large"));

        assertThrows(DuplicateDogException.class, () -> uniqueDogList.setDogs(List.of(firstDog, secondDog)));
    }
}
