package seedu.address;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.ReadOnlyWatchDog;
import seedu.address.model.UserPrefs;
import seedu.address.model.WatchDog;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.JsonWatchDogStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path WATCHDOG_DATA_FILE_PATH = Paths.get("data", "watchdog.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private final Path userPrefsFilePath;
    private final Path dataFilePath;

    /**
     * Creates an application using the default preferences and data files.
     */
    public MainApp() {
        this(USER_PREFS_FILE_PATH, WATCHDOG_DATA_FILE_PATH);
    }

    /**
     * Creates an application using the given preferences and data files.
     */
    MainApp(Path userPrefsFilePath, Path dataFilePath) {
        this.userPrefsFilePath = requireNonNull(userPrefsFilePath);
        this.dataFilePath = requireNonNull(dataFilePath);
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing WatchDog ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsFilePath);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonWatchDogStorage watchDogStorage = new JsonWatchDogStorage(dataFilePath);
        storage = new StorageManager(watchDogStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getWatchDogFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s WatchDog and {@code userPrefs}. <br>
     * The data from the sample WatchDog will be used instead if {@code storage}'s WatchDog is not found,
     * or an empty WatchDog data file will be used instead if errors occur when reading {@code storage}'s WatchDog.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getWatchDogFilePath());

        Optional<ReadOnlyWatchDog> watchDogOptional;
        ReadOnlyWatchDog initialData;
        try {
            watchDogOptional = storage.readWatchDog();
            if (watchDogOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getWatchDogFilePath()
                        + " populated with sample WatchDog data.");
            }
            initialData = watchDogOptional.orElseGet(SampleDataUtil::getSampleWatchDog);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getWatchDogFilePath() + " could not be loaded."
                    + " Will be starting with no WatchDog records.");
            initialData = new WatchDog();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting WatchDog " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping WatchDog ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
