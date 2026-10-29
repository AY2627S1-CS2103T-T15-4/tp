package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedDog.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Contains tests for {@link JsonAdaptedDog}. */
public class JsonAdaptedDogTest {
    private static final Dog VALID_DOG = new Dog(1, new DogName("Cookie"),
            new Breed("Poodle"), new Size("Large"));

    @Test
    public void toModelType_validDogDetails_returnsDog() throws Exception {
        assertEquals(VALID_DOG, new JsonAdaptedDog(VALID_DOG).toModelType());
    }

    @Test
    public void toModelType_nullId_throwsIllegalValueException() {
        JsonAdaptedDog dog = new JsonAdaptedDog(null, "Cookie", "Poodle", "Large", null);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"), dog::toModelType);
    }

    @Test
    public void toModelType_negativeId_throwsIllegalValueException() {
        JsonAdaptedDog dog = new JsonAdaptedDog(-1, "Cookie", "Poodle", "Large", null);
        assertThrows(IllegalValueException.class, "Dog id must be nonnegative.", dog::toModelType);
    }

    @Test
    public void toModelType_nonPositiveOwnerId_throwsIllegalValueException() {
        for (int ownerId : new int[]{-1, 0}) {
            JsonAdaptedDog dog = new JsonAdaptedDog(1, "Cookie", "Poodle", "Large", ownerId);
            assertThrows(IllegalValueException.class, "Dog owner ID must be positive.", dog::toModelType);
        }
    }

    @Test
    public void toModelType_invalidOrMissingName_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, DogName.MESSAGE_CONSTRAINTS,
                new JsonAdaptedDog(1, "", "Poodle", "Large", null)::toModelType);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"),
                new JsonAdaptedDog(1, null, "Poodle", "Large", null)::toModelType);
    }

    @Test
    public void toModelType_invalidOrMissingBreed_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Breed.MESSAGE_CONSTRAINTS,
                new JsonAdaptedDog(1, "Cookie", "", "Large", null)::toModelType);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "breed"),
                new JsonAdaptedDog(1, "Cookie", null, "Large", null)::toModelType);
    }

    @Test
    public void toModelType_invalidOrMissingSize_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Size.MESSAGE_CONSTRAINTS,
                new JsonAdaptedDog(1, "Cookie", "Poodle", "", null)::toModelType);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "size"),
                new JsonAdaptedDog(1, "Cookie", "Poodle", null, null)::toModelType);
    }
}
