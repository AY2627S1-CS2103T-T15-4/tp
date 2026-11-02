package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyWatchDog;

/**
 * A class to access WatchDog data stored as a JSON file on the hard disk.
 */
public class JsonWatchDogStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonWatchDogStorage.class);

    private Path filePath;
    private final Set<Path> unreadableFiles = new HashSet<>();

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

        try {
            Optional<JsonSerializableWatchDog> jsonWatchDog = JsonUtil.readJsonFile(
                    filePath, JsonSerializableWatchDog.class);
            if (!jsonWatchDog.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(jsonWatchDog.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            unreadableFiles.add(filePath.toAbsolutePath().normalize());
            throw new DataLoadingException(ive);
        } catch (DataLoadingException e) {
            unreadableFiles.add(filePath.toAbsolutePath().normalize());
            throw e;
        }
    }

    /**
     * Saves the given {@link ReadOnlyWatchDog} to the storage.
     * Preserves a uniquely named backup before replacing data that failed to load.
     * @param watchDog cannot be null.
     * @throws IOException if backup creation or writing fails.
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

        Path destination = filePath.toAbsolutePath().normalize();
        if (unreadableFiles.contains(destination)) {
            Path backup = destination.resolveSibling(destination.getFileName()
                    + ".invalid-" + UUID.randomUUID() + ".bak");
            Files.copy(destination, backup);
            unreadableFiles.remove(destination);
            logger.warning("Preserved unreadable data in " + backup);
        }
        JsonUtil.saveJsonFile(new JsonSerializableWatchDog(watchDog), filePath);
    }

}
