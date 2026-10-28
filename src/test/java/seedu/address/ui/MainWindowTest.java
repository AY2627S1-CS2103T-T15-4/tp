package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;
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
        model.addActivity(new ActivityName("Walk"), LocalDate.of(2028, 2, 29),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Today walk"), LocalDate.now(),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            ListView<Supplier<Region>> customers = listView(window.getCustomerListPanel());
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            ListView<Supplier<Region>> activities = listView(window.getActivityListPanel());
            StackPane todayPanelPlaceholder = (StackPane) window.getPrimaryStage().getScene()
                    .lookup("#todayActivityListPanelPlaceholder");
            Label todayHeader = (Label) window.getPrimaryStage().getScene().lookup("#todayActivitiesHeader");
            @SuppressWarnings("unchecked")
            ListView<Supplier<Region>> todaysActivities = (ListView<Supplier<Region>>) todayPanelPlaceholder
                    .lookup("#recordListView");
            assertEquals(1, customers.getItems().size());
            assertEquals(1, dogs.getItems().size());
            assertEquals(2, activities.getItems().size());
            assertEquals("Walk", label(activities.getItems().get(0).get(), "name"));
            assertTrue(todayHeader.getText().startsWith("Today · "));
            assertEquals(1, todaysActivities.getItems().size());
            assertEquals("Today walk", label(todaysActivities.getItems().get(0).get(), "name"));

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

    @Test
    public void executeCommand_resetDisplay_restoresBothListsAndShowsUpcomingActivities() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-reset");
        LocalDate today = LocalDate.now();
        Model model = new ModelManager();
        model.addCustomer(LILY);
        model.addDog(new Dog(new DogName("Cookie"), new Breed("Chihuahua"), new Size("Small")));
        model.addActivity(new ActivityName("Past walk"), today.minusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Daily walk"), today.minusDays(2),
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.DAILY);
        model.addActivity(new ActivityName("Today walk"), today,
                LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.ONCE);
        model.addActivity(new ActivityName("Future walk"), today.plusDays(1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            ListView<Supplier<Region>> customers = listView(window.getCustomerListPanel());
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            ListView<Supplier<Region>> activities = listView(window.getActivityListPanel());

            window.executeCommand("list dog");
            assertTrue(customers.getItems().isEmpty());
            window.executeCommand("reset");

            assertEquals(1, customers.getItems().size());
            assertEquals(1, dogs.getItems().size());
            assertEquals(3, activities.getItems().size());
            assertEquals("Daily walk", label(activities.getItems().get(0).get(), "name"));
            assertEquals("Today walk", label(activities.getItems().get(1).get(), "name"));
            assertEquals("Future walk", label(activities.getItems().get(2).get(), "name"));
        });
    }

    @Test
    public void fillInnerParts_resizesPawPatternAndRendersPawMarks() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-paw-pattern");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            Pane pawPattern = (Pane) window.getPrimaryStage().getScene().lookup("#pawPattern");

            pawPattern.resize(300, 200);

            int initialPawCount = pawPattern.getChildren().size();
            assertTrue(initialPawCount > 0);
            Group paw = (Group) pawPattern.getChildren().get(0);
            assertEquals(5, paw.getChildren().size());
            assertTrue(paw.getChildren().get(0) instanceof Ellipse);
            assertTrue(paw.getChildren().get(1) instanceof Circle);
            assertEquals(0.20, paw.getOpacity());

            pawPattern.resize(420, 200);

            assertTrue(pawPattern.getChildren().size() > initialPawCount);
            window.getPrimaryStage().close();
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
