package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalWatchDog.getTypicalWatchDog;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyWatchDog;
import seedu.address.model.WatchDog;
import seedu.address.model.customer.Customer;

public class JsonWatchDogStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonWatchDogStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readWatchDog_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readWatchDog(null));
    }

    private java.util.Optional<ReadOnlyWatchDog> readWatchDog(String filePath) throws Exception {
        return new JsonWatchDogStorage(Paths.get(filePath)).readWatchDog(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readWatchDog("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readWatchDog("notJsonFormatWatchDog.json"));
    }

    @Test
    public void readWatchDog_invalidCustomerWatchDog_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readWatchDog("invalidCustomerWatchDog.json"));
    }

    @Test
    public void readWatchDog_invalidAndValidCustomerWatchDog_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readWatchDog("invalidAndValidCustomerWatchDog.json"));
    }

    @Test
    public void readAndSaveWatchDog_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempWatchDog.json");
        WatchDog original = getTypicalWatchDog();
        JsonWatchDogStorage jsonWatchDogStorage = new JsonWatchDogStorage(filePath);

        // Save in new file and read back
        jsonWatchDogStorage.saveWatchDog(original, filePath);
        ReadOnlyWatchDog readBack = jsonWatchDogStorage.readWatchDog(filePath).get();
        assertEquals(original, new WatchDog(readBack));

        // Modify data, overwrite existing file, and read back
        original.addCustomer(new Customer(20, "Carol", "94567890", "carol@example.com", "95678901"));
        original.removeCustomer(original.getCustomerList().get(0));
        jsonWatchDogStorage.saveWatchDog(original, filePath);
        readBack = jsonWatchDogStorage.readWatchDog(filePath).get();
        assertEquals(original, new WatchDog(readBack));

        // Save and read without specifying file path
        original.addCustomer(new Customer(21, "Dan", "95678901", "dan@example.com", "96789012"));
        jsonWatchDogStorage.saveWatchDog(original); // file path not specified
        readBack = jsonWatchDogStorage.readWatchDog().get(); // file path not specified
        assertEquals(original, new WatchDog(readBack));

    }

    @Test
    public void saveWatchDog_nullWatchDog_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveWatchDog(null, "SomeFile.json"));
    }

    /**
     * Saves {@code watchDog} at the specified {@code filePath}.
     */
    private void saveWatchDog(ReadOnlyWatchDog watchDog, String filePath) {
        try {
            new JsonWatchDogStorage(Paths.get(filePath))
                    .saveWatchDog(watchDog, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveWatchDog_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveWatchDog(new WatchDog(), null));
    }
}
