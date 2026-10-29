package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyWatchDog;

/**
 * A class to access WatchDog data stored as a JSON file on the hard disk.
 */
public class JsonWatchDogStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonWatchDogStorage.class);

    private Path filePath;

    public JsonWatchDogStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getWatchDogFilePath() {
        return filePath;
    }

    /**
     * Returns WatchDog data as a {@link ReadOnlyWatchDog}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyWatchDog> readWatchDog() throws DataLoadingException {
        return readWatchDog(filePath);
    }

    /**
     * Similar to {@link #readWatchDog()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyWatchDog> readWatchDog(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableWatchDog> jsonWatchDog = JsonUtil.readJsonFile(
                filePath, JsonSerializableWatchDog.class);
        if (!jsonWatchDog.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonWatchDog.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyWatchDog} to the storage.
     * @param watchDog cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveWatchDog(ReadOnlyWatchDog watchDog) throws IOException {
        saveWatchDog(watchDog, filePath);
    }

    /**
     * Similar to {@link #saveWatchDog(ReadOnlyWatchDog)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveWatchDog(ReadOnlyWatchDog watchDog, Path filePath) throws IOException {
        requireNonNull(watchDog);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableWatchDog(watchDog), filePath);
    }

}
