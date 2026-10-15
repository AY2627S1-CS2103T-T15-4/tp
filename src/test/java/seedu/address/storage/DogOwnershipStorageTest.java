package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

public class DogOwnershipStorageTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndRead_deletedCustomer_preservesDogsWithoutOwnership() throws Exception {
        AddressBook book = new AddressBook();
        Customer owner = new Customer(7, "Alex", "91234567", "alex@example.com", "98765432");
        book.addCustomer(owner);
        book.addDogFromStorage(dog(42));
        book.addDogFromStorage(dog(43));
        book.addDogFromStorage(dog(44));
        book.linkDogs(7, List.of(42, 43));
        Path file = temporaryFolder.resolve("ownership.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(book);
        AddressBook restored = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(book, restored);
        assertEquals(Optional.of(7), restored.getDogList().get(0).getOwnerId());
        assertEquals(Optional.of(7), restored.getDogList().get(1).getOwnerId());
        assertTrue(restored.getDogList().get(2).getOwnerId().isEmpty());
        restored.removeCustomer(owner);
        storage.saveAddressBook(restored);
        var reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(List.of(dog(42), dog(43), dog(44)), reloaded.getDogList());
        assertTrue(reloaded.getCustomerList().isEmpty());
    }

    @Test
    public void read_legacyDogWithoutOwner_loadsUnlinked() throws Exception {
        AddressBook book = readDog("").toModelType();
        assertTrue(book.getDogList().get(0).getOwnerId().isEmpty());
        assertEquals(dog(42), book.getDogList().get(0));
    }

    @Test
    public void read_negativeOrMissingOwner_rejectsInvalidAssociation() throws Exception {
        JsonSerializableAddressBook negative = readDog(", \"ownerId\": -1");
        assertThrows(IllegalValueException.class, negative::toModelType);
        JsonSerializableAddressBook missing = readDog(", \"ownerId\": 7");
        assertThrows(IllegalValueException.class, missing::toModelType);
    }

    private JsonSerializableAddressBook readDog(String ownerField) throws Exception {
        Path file = temporaryFolder.resolve("input.json");
        Files.writeString(file, "{\"dogs\":[{\"id\":42,\"name\":\"Buddy\",\"breed\":\"Poodle\","
                + "\"size\":\"small\"" + ownerField + "}]}");
        return JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).orElseThrow();
    }

    private Dog dog(int id) {
        return new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    }
}
