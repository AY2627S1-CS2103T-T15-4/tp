package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.ReadOnlyWatchDog;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of WatchDog data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonWatchDogStorage watchDogStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given WatchDog and user prefs storage.
     */
    public StorageManager(JsonWatchDogStorage watchDogStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.watchDogStorage = watchDogStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ WatchDog methods ==============================

    @Override
    public Path getWatchDogFilePath() {
        return watchDogStorage.getWatchDogFilePath();
    }

    @Override
    public Optional<ReadOnlyWatchDog> readWatchDog() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + watchDogStorage.getWatchDogFilePath());
        return watchDogStorage.readWatchDog();
    }

    @Override
    public void saveWatchDog(ReadOnlyWatchDog watchDog) throws IOException {
        logger.fine("Attempting to write to data file: " + watchDogStorage.getWatchDogFilePath());
        watchDogStorage.saveWatchDog(watchDog);
    }

}
