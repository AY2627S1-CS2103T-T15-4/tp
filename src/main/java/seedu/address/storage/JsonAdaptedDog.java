package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Jackson-friendly version of {@link Dog}. */
class JsonAdaptedDog {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Dog's %s field is missing!";

    private final Integer id;
    private final String name;
    private final String breed;
    private final String size;
    private final Integer ownerId;

    @JsonCreator
    public JsonAdaptedDog(@JsonProperty("id") Integer id, @JsonProperty("name") String name,
            @JsonProperty("breed") String breed, @JsonProperty("size") String size,
            @JsonProperty("ownerId") Integer ownerId) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.size = size;
        this.ownerId = ownerId;
    }

    public JsonAdaptedDog(Dog source) {
        id = source.getId();
        name = source.getName().toString();
        breed = source.getBreed().toString();
        size = source.getSize().toString();
        ownerId = source.getOwnerId().orElse(null);
    }

    public Dog toModelType() throws IllegalValueException {
        if (id == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "id"));
        }
        if (id < 0) {
            throw new IllegalValueException("Dog id must be nonnegative.");
        }
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!DogName.isValidName(name)) {
            throw new IllegalValueException(DogName.MESSAGE_CONSTRAINTS);
        }
        if (breed == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "breed"));
        }
        if (!Breed.isValidBreed(breed)) {
            throw new IllegalValueException(Breed.MESSAGE_CONSTRAINTS);
        }
        if (size == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "size"));
        }
        if (!Size.isValidSize(size)) {
            throw new IllegalValueException(Size.MESSAGE_CONSTRAINTS);
        }

        if (ownerId != null && ownerId < 1) {
            throw new IllegalValueException("Dog owner ID must be positive.");
        }
        return new Dog(id, new DogName(name), new Breed(breed), new Size(size), ownerId);
    }
}
