package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowTest {
    private static final Customer LILY = new Customer(1, "Lily Koh", "91234567", "lily@gmail.com", "98765432");

    @BeforeAll
    public static void startJavaFx() throws Exception {
        assumeTrue(!System.getProperty("os.name").startsWith("Linux") || System.getenv("DISPLAY") != null,
                "JavaFX UI tests require a display (use xvfb-run on headless Linux)");
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

    @Test
    public void executeCommand_listCustomerAndListDog_switchesVisibleRecords() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow");
        Model model = new ModelManager();
        model.addCustomer(LILY);
        model.addDog(new Dog(new DogName("Cookie"), new Breed("Chihuahua"), new Size("Small")));
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            ListView<Supplier<Region>> customers = listView(window.getCustomerListPanel());
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            assertEquals(1, customers.getItems().size());
            assertEquals(1, dogs.getItems().size());

            window.executeCommand("list customer");
            assertEquals(1, customers.getItems().size());
            assertEquals("Lily Koh", label(customers.getItems().get(0).get(), "name"));

            window.executeCommand("search dog n/Cookie");
            assertEquals(1, dogs.getItems().size());
            assertEquals("Cookie", label(dogs.getItems().get(0).get(), "name"));

            window.executeCommand("search customer n/Lily");
            assertEquals(1, customers.getItems().size());
            assertEquals("Lily Koh", label(customers.getItems().get(0).get(), "name"));

            window.executeCommand("list dog");
            assertEquals(1, dogs.getItems().size());
            assertEquals("Cookie", label(dogs.getItems().get(0).get(), "name"));
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Supplier<Region>> listView(UiPart<Region> panel) {
        return (ListView<Supplier<Region>>) panel.getRoot().lookup("#recordListView");
    }

    private static void runOnFxThread(ThrowingRunnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            assertions.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
