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

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.WatchDog;
// @@author Kenneth-Chia
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
        // @@author
        WatchDog book = new WatchDog();
        // @@author Kenneth-Chia
        Customer owner = new Customer(7, "Alex", "91234567", "alex@example.com", "98765432");
        book.addCustomer(owner);
        book.addDogFromStorage(dog(42));
        book.addDogFromStorage(dog(43));
        book.addDogFromStorage(dog(44));
        book.linkDogs(7, List.of(42, 43));
        Path file = temporaryFolder.resolve("ownership.json");
        // @@author
        JsonWatchDogStorage storage = new JsonWatchDogStorage(file);
        storage.saveWatchDog(book);
        WatchDog restored = new WatchDog(storage.readWatchDog().orElseThrow());
        // @@author Kenneth-Chia
        assertEquals(book, restored);
        assertEquals(Optional.of(7), restored.getDogList().get(0).getOwnerId());
        assertEquals(Optional.of(7), restored.getDogList().get(1).getOwnerId());
        assertTrue(restored.getDogList().get(2).getOwnerId().isEmpty());
        restored.removeCustomer(owner);
        // @@author
        storage.saveWatchDog(restored);
        var reloaded = storage.readWatchDog().orElseThrow();
        // @@author Kenneth-Chia
        assertEquals(List.of(dog(42), dog(43), dog(44)), reloaded.getDogList());
        assertTrue(reloaded.getCustomerList().isEmpty());
    }

    @Test
    public void saveAndRead_unlinkedDog_preservesCustomerAndRemainingOwnership() throws Exception {
        // @@author
        WatchDog book = new WatchDog();
        // @@author Kenneth-Chia
        Customer owner = new Customer(7, "Alex", "91234567", "alex@example.com", "98765432");
        book.addCustomer(owner);
        book.addDogFromStorage(dog(42));
        book.addDogFromStorage(dog(43));
        book.linkDogs(7, List.of(42, 43));
        book.unlinkDogs(List.of(42));
        // @@author
        JsonWatchDogStorage storage = new JsonWatchDogStorage(temporaryFolder.resolve("unlink.json"));
        storage.saveWatchDog(book);
        var restored = storage.readWatchDog().orElseThrow();
        // @@author Kenneth-Chia
        assertEquals(List.of(dog(42), dog(43).withOwner(7)), restored.getDogList());
        assertEquals(List.of(owner), restored.getCustomerList());
        book.unlinkCustomerDogs(7);
        // @@author
        storage.saveWatchDog(book);
        var allUnlinked = storage.readWatchDog().orElseThrow();
        // @@author Kenneth-Chia
        assertEquals(List.of(dog(42), dog(43)), allUnlinked.getDogList());
        assertEquals(List.of(owner), allUnlinked.getCustomerList());
    }

    @Test
    public void read_legacyDogWithoutOwner_loadsUnlinked() throws Exception {
        // @@author
        WatchDog book = readDog("").toModelType();
        // @@author Kenneth-Chia
        assertTrue(book.getDogList().get(0).getOwnerId().isEmpty());
        assertEquals(dog(42), book.getDogList().get(0));
    }

    @Test
    public void read_negativeOrMissingOwner_rejectsInvalidAssociation() throws Exception {
        // @@author
        JsonSerializableWatchDog negative = readDog(", \"ownerId\": -1");
        assertThrows(IllegalValueException.class, negative::toModelType);
        JsonSerializableWatchDog missing = readDog(", \"ownerId\": 7");
        // @@author Kenneth-Chia
        assertThrows(IllegalValueException.class, missing::toModelType);
    }

    @Test
    public void read_nonIntegerOwnerId_rejectsWithoutChangingFile() throws Exception {
        Path file = temporaryFolder.resolve("malformed-owner.json");
        JsonWatchDogStorage storage = new JsonWatchDogStorage(file);
        for (String token : List.of("7.5", "\"7\"", "true", "{}", "[]", "2147483648", "7e0")) {
            String json = "{\"customers\":[{\"id\":7,\"name\":\"Alex\",\"phone\":\"91234567\","
                    + "\"email\":\"alex@example.com\",\"backupContact\":\"98765432\"}],"
                    + "\"dogs\":[{\"id\":42,\"name\":\"Buddy\",\"breed\":\"Poodle\",\"size\":\"small\","
                    + "\"ownerId\":" + token + "}]}";
            Files.writeString(file, json);

            assertThrows(DataLoadingException.class, storage::readWatchDog, token);
            assertEquals(json, Files.readString(file));
        }
    }

    @Test
    public void read_nullOwnerId_loadsUnlinked() throws Exception {
        assertEquals(dog(42), readDog(", \"ownerId\": null").toModelType().getDogList().get(0));
    }
    // @@author

    private JsonSerializableWatchDog readDog(String ownerField) throws Exception {
        // @@author Kenneth-Chia
        Path file = temporaryFolder.resolve("input.json");
        Files.writeString(file, "{\"dogs\":[{\"id\":42,\"name\":\"Buddy\",\"breed\":\"Poodle\","
                + "\"size\":\"small\"" + ownerField + "}]}");
        // @@author
        return JsonUtil.readJsonFile(file, JsonSerializableWatchDog.class).orElseThrow();
    }

    private Dog dog(int id) {
        return new Dog(id, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    }
}
