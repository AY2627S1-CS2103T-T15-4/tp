package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalWatchDog.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void init_missingFiles_loadsSamplesAndPersistsCommandAcrossRestart() throws Exception {
        MainApp app = createApp();
        app.init();

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(new UserPrefs(), prefsStorage().readUserPrefs().orElseThrow());
        assertEquals(dataPath(), app.storage.getAddressBookFilePath());
        assertEquals(prefsPath(), app.storage.getUserPrefsFilePath());
        assertFalse(Files.exists(dataPath()));

        app.logic.execute("adddog n/Cookie b/Poodle s/Small");
        AddressBook saved = new AddressBook(app.model.getAddressBook());
        int nextDogId = saved.getNextDogId();
        assertEquals(saved, dataStorage().readAddressBook().orElseThrow());

        MainApp restarted = createApp();
        restarted.init();
        assertEquals(saved, restarted.model.getAddressBook());
        assertEquals(nextDogId, restarted.model.getAddressBook().getNextDogId());
    }

    @Test
    public void init_savedFiles_restoresRecordsAndPreferencesWithoutChangingDataFile() throws Exception {
        AddressBook saved = getTypicalAddressBook();
        UserPrefs preferences = customPrefs();
        dataStorage().saveAddressBook(saved);
        prefsStorage().saveUserPrefs(preferences);
        byte[] originalData = Files.readAllBytes(dataPath());

        MainApp app = createApp();
        app.init();

        assertEquals(saved, app.model.getAddressBook());
        assertEquals(preferences, app.model.getUserPrefs());
        assertArrayEquals(originalData, Files.readAllBytes(dataPath()));
    }

    @Test
    public void init_malformedData_startsEmptyAndPreservesFileUntilCommand() throws Exception {
        String invalidData = "{not valid json";
        Files.createDirectories(dataPath().getParent());
        Files.writeString(dataPath(), invalidData);

        MainApp app = createApp();
        app.init();

        assertEquals(new AddressBook(), app.model.getAddressBook());
        assertEquals(invalidData, Files.readString(dataPath()));

        app.logic.execute("adddog n/Cookie b/Poodle s/Small");
        assertEquals(1, app.model.getDogList().size());
        assertEquals(app.model.getAddressBook(), dataStorage().readAddressBook().orElseThrow());
    }

    @Test
    public void init_malformedPreferences_keepsRecordsAndWritesDefaultPreferences() throws Exception {
        AddressBook saved = getTypicalAddressBook();
        dataStorage().saveAddressBook(saved);
        Files.writeString(prefsPath(), "{not valid json");

        MainApp app = createApp();
        app.init();

        assertEquals(saved, app.model.getAddressBook());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(new UserPrefs(), prefsStorage().readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_saveFailure_returnsLoadedPreferencesAndLeavesSavedFileUnchanged() throws Exception {
        UserPrefs preferences = customPrefs();
        prefsStorage().saveUserPrefs(preferences);
        byte[] originalPreferences = Files.readAllBytes(prefsPath());
        FailingUserPrefsStorage failingStorage = new FailingUserPrefsStorage(prefsPath());

        UserPrefs loaded = new MainApp().initPrefs(failingStorage);

        assertEquals(preferences, loaded);
        assertEquals(preferences, failingStorage.attemptedPreferences);
        assertArrayEquals(originalPreferences, Files.readAllBytes(prefsPath()));
    }

    @Test
    public void start_passesPrimaryStageToUi() throws Exception {
        startJavaFx();
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            AtomicReference<Stage> receivedStage = new AtomicReference<>();
            MainApp app = new MainApp();
            app.ui = receivedStage::set;
            try {
                app.start(stage);
                assertSame(stage, receivedStage.get());
            } finally {
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void stop_changedGuiSettings_persistsLatestPreferences() throws Exception {
        MainApp app = createApp();
        app.init();
        UserPrefs preferences = customPrefs();
        app.model.setGuiSettings(preferences.getGuiSettings());

        app.stop();

        assertEquals(preferences, prefsStorage().readUserPrefs().orElseThrow());
    }

    @Test
    public void stop_saveFailure_preservesModelAndExistingPreferences() throws Exception {
        MainApp app = createApp();
        app.init();
        UserPrefs preferences = customPrefs();
        app.model.setGuiSettings(preferences.getGuiSettings());
        AddressBook originalData = new AddressBook(app.model.getAddressBook());
        byte[] originalPreferences = Files.readAllBytes(prefsPath());
        FailingUserPrefsStorage failingStorage = new FailingUserPrefsStorage(prefsPath());
        app.storage = new StorageManager(dataStorage(), failingStorage);

        assertDoesNotThrow(app::stop);

        assertEquals(preferences, failingStorage.attemptedPreferences);
        assertEquals(preferences, app.model.getUserPrefs());
        assertEquals(originalData, app.model.getAddressBook());
        assertArrayEquals(originalPreferences, Files.readAllBytes(prefsPath()));
    }

    private MainApp createApp() {
        return new MainApp(prefsPath(), dataPath());
    }

    private Path prefsPath() {
        return temporaryFolder.resolve("preferences.json");
    }

    private Path dataPath() {
        return temporaryFolder.resolve("data").resolve("watchdog.json");
    }

    private JsonUserPrefsStorage prefsStorage() {
        return new JsonUserPrefsStorage(prefsPath());
    }

    private JsonAddressBookStorage dataStorage() {
        return new JsonAddressBookStorage(dataPath());
    }

    private static UserPrefs customPrefs() {
        UserPrefs preferences = new UserPrefs();
        preferences.setGuiSettings(new GuiSettings(1200, 700, 30, 50));
        return preferences;
    }

    private static void startJavaFx() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX lifecycle tests require a display (use xvfb-run on headless Linux)");
        try {
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
        } catch (IllegalStateException alreadyStarted) {
            Platform.setImplicitExit(false);
        }
    }

    private static class FailingUserPrefsStorage extends JsonUserPrefsStorage {
        private ReadOnlyUserPrefs attemptedPreferences;

        FailingUserPrefsStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs preferences) throws IOException {
            attemptedPreferences = new UserPrefs(preferences);
            throw new IOException("Preference save failed for this test");
        }
    }
}
