package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
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
    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 10, 28);
    private static final LocalDate FAR_FUTURE = LocalDate.of(9999, 12, 31);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-28T04:00:00Z"), ZoneOffset.UTC);

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
                new JsonAddressBookStorage(folder.resolve("watchdog.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("watchdog.json"));
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

            window.executeCommand("reset");
            assertEquals(1, customers.getItems().size());
            assertEquals("Lily Koh", label(customers.getItems().get(0).get(), "name"));

            window.executeCommand("searchd n/Cookie");
            assertEquals(1, dogs.getItems().size());
            assertEquals("Cookie", label(dogs.getItems().get(0).get(), "name"));

            window.executeCommand("searchc n/Lily");
            assertEquals(1, customers.getItems().size());
            assertEquals("Lily Koh", label(customers.getItems().get(0).get(), "name"));

            window.executeCommand("searchd n/Cookie");
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
                new JsonAddressBookStorage(folder.resolve("watchdog.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("watchdog.json"));
            window.fillInnerParts();
            ListView<Supplier<Region>> customers = listView(window.getCustomerListPanel());
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            ListView<Supplier<Region>> activities = listView(window.getActivityListPanel());

            window.executeCommand("searchd n/Cookie");
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
    public void activityBookingSummaries_ignoreDogFilterAndRefreshAcrossCommands() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-bookings");
        AddressBook book = new AddressBook();
        book.addDogFromStorage(new Dog(1, new DogName("Cookie"), new Breed("Chihuahua"), new Size("Small")));
        book.addDogFromStorage(new Dog(2, new DogName("Milo"), new Breed("Poodle"), new Size("Medium")));
        book.addActivity(new ActivityName("Today walk"), FIXED_TODAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
        book.addActivity(new ActivityName("Future walk"), FAR_FUTURE,
                LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.ONCE);
        book.addEnrolmentFromStorage(new Enrolment(1, 1, FIXED_TODAY));
        Model model = new ModelManager(book, new UserPrefs());
        model.updateFilteredDogList(unused -> false);
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"), FIXED_CLOCK);
            window.fillInnerParts();
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            ListView<Supplier<Region>> activities = listView(window.getActivityListPanel());
            StackPane todayPlaceholder = (StackPane) window.getPrimaryStage().getScene()
                    .lookup("#todayActivityListPanelPlaceholder");
            @SuppressWarnings("unchecked")
            ListView<Supplier<Region>> todayActivities = (ListView<Supplier<Region>>) todayPlaceholder
                    .lookup("#recordListView");

            assertTrue(dogs.getItems().isEmpty());
            assertEquals("Dogs with upcoming bookings: 1",
                    label(activities.getItems().get(0).get(), "dogSummary"));
            assertEquals("Dogs enrolled on " + FIXED_TODAY + ": 1",
                    label(todayActivities.getItems().get(0).get(), "dogSummary"));
            assertEquals("Today · 28 Oct 2026", ((Label) window.getPrimaryStage().getScene()
                    .lookup("#todayActivitiesHeader")).getText());
            assertEquals("Dogs with upcoming bookings: None",
                    label(activities.getItems().get(1).get(), "dogSummary"));

            window.executeCommand("enrol a/2 date/" + FAR_FUTURE + " d/2");
            assertEquals("Dogs with upcoming bookings: 2",
                    label(activities.getItems().get(1).get(), "dogSummary"));
            window.executeCommand("unenrol a/2 date/" + FAR_FUTURE + " d/2");
            assertEquals("Dogs with upcoming bookings: None",
                    label(activities.getItems().get(1).get(), "dogSummary"));

            window.executeCommand("unenrol a/1 date/" + FIXED_TODAY + " d/1");
            assertEquals("Dogs with upcoming bookings: None",
                    label(activities.getItems().get(0).get(), "dogSummary"));
            assertEquals("Dogs enrolled on " + FIXED_TODAY + ": None",
                    label(todayActivities.getItems().get(0).get(), "dogSummary"));

            window.executeCommand("undo");
            assertEquals("Dogs with upcoming bookings: 1",
                    label(activities.getItems().get(0).get(), "dogSummary"));
            window.executeCommand("redo");
            assertEquals("Dogs with upcoming bookings: None",
                    label(activities.getItems().get(0).get(), "dogSummary"));

            window.executeCommand("undo");
            window.executeCommand("delete dog 1");
            assertEquals("Dogs with upcoming bookings: None",
                    label(activities.getItems().get(0).get(), "dogSummary"));

            window.executeCommand("clear");
            window.executeCommand("Y");
            assertTrue(activities.getItems().isEmpty());
            assertTrue(todayActivities.getItems().isEmpty());
        });
    }

    @Test
    public void fillInnerParts_resizesPawPatternAndRendersPawMarks() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-paw-pattern");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("watchdog.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("watchdog.json"));
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

    @Test
    public void fillInnerParts_usesFriendlyInputAndResultPlaceholders() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-placeholders");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            TextField command = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
            TextArea result = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
            StackPane resultArea = (StackPane) window.getPrimaryStage().getScene()
                    .lookup("#resultDisplayPlaceholder");

            window.show();
            window.getPrimaryStage().getScene().getRoot().applyCss();

            assertEquals("What would you like to do? Type help to get started.", command.getPromptText());
            assertEquals("Command results will appear here.", result.getPromptText());
            assertEquals(11 * 96.0 / 72.0, command.getFont().getSize(), 0.01);
            assertEquals(12 * 96.0 / 72.0, result.getFont().getSize(), 0.01);
            assertTrue(result.isWrapText());
            assertEquals(100, resultArea.getMinHeight());
            assertEquals(100, resultArea.getPrefHeight());
            assertEquals(100, resultArea.getMaxHeight());
            assertEquals(0.70, listView(window.getCustomerListPanel()).getPlaceholder().getOpacity());
            assertEquals(0.70, listView(window.getDogListPanel()).getPlaceholder().getOpacity());
            assertEquals(0.70, listView(window.getActivityListPanel()).getPlaceholder().getOpacity());
            window.getPrimaryStage().close();
        });
    }

    @Test
    public void menuDropdowns_useReadableThemeColorsForNormalHoverAndFocusStates() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-menu-theme");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            window.show();
            try {
                MenuBar menuBar = (MenuBar) window.getPrimaryStage().getScene().lookup("#menuBar");
                for (Menu menu : menuBar.getMenus()) {
                    menu.show();
                    MenuItem item = menu.getItems().get(0);
                    Node itemNode = item.getStyleableNode();
                    assertNotNull(itemNode);
                    itemNode.getScene().getRoot().applyCss();
                    Label itemLabel = (Label) itemNode.lookup(".label");
                    Region itemRegion = (Region) itemNode;

                    assertNotNull(itemLabel);
                    assertEquals(Color.web("#096B68"), itemLabel.getTextFill());

                    PseudoClass hover = PseudoClass.getPseudoClass("hover");
                    itemNode.pseudoClassStateChanged(hover, true);
                    itemNode.applyCss();
                    assertEquals(Color.web("#90D1CA"), itemRegion.getBackground()
                            .getFills().get(0).getFill());
                    assertEquals(Color.web("#096B68"), itemLabel.getTextFill());
                    itemNode.pseudoClassStateChanged(hover, false);

                    PseudoClass focused = PseudoClass.getPseudoClass("focused");
                    itemNode.pseudoClassStateChanged(focused, true);
                    itemNode.applyCss();
                    assertEquals(Color.web("#90D1CA"), itemRegion.getBackground()
                            .getFills().get(0).getFill());
                    assertEquals(Color.web("#096B68"), itemLabel.getTextFill());
                    itemNode.pseudoClassStateChanged(focused, false);
                    menu.hide();
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void executeCommand_failureHelpAndExit_updateVisibleWindowState() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-command-results");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            window.show();
            TextArea result = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");

            assertThrows(ParseException.class, () -> window.executeCommand("not-a-command"));
            assertTrue(result.getText().contains("Unknown command"));

            window.executeCommand("help");
            assertTrue(Window.getWindows().stream().filter(Window::isShowing).count() >= 2);

            window.executeCommand("exit");
            assertFalse(window.getPrimaryStage().isShowing());
            assertFalse(Window.getWindows().stream().anyMatch(Window::isShowing));
        });
    }

    @Test
    public void executeCommand_displayChangingCommandResetsListsToTop() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-scroll-reset");
        Model model = new ModelManager();
        for (int index = 1; index <= 20; index++) {
            model.addDog(new Dog(new DogName("Dog " + index), new Breed("Poodle"), new Size("Small")));
        }
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            window.show();
            ListView<Supplier<Region>> dogs = listView(window.getDogListPanel());
            dogs.scrollTo(dogs.getItems().size() - 1);
            dogs.layout();
            assertTrue(firstVisibleIndex(dogs) > 0);

            window.executeCommand("reset");
            dogs.layout();

            assertEquals(0, firstVisibleIndex(dogs));
            window.getPrimaryStage().close();
        });
    }

    @Test
    public void helpAccelerator_textInputHasFocus_opensHelpWindow() throws Exception {
        Path folder = Files.createTempDirectory("mainwindow-help-accelerator");
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(folder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(folder.resolve("userprefs.json"))));

        runOnFxThread(() -> {
            MainWindow window = new MainWindow(new Stage(), logic, folder.resolve("addressbook.json"));
            window.fillInnerParts();
            window.show();
            TextField command = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
            command.requestFocus();

            command.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                    false, false, false, false));

            assertTrue(Window.getWindows().stream().filter(Window::isShowing).count() >= 2);
            window.executeCommand("exit");
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    private static int firstVisibleIndex(ListView<?> listView) {
        return listView.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(ListCell.class::cast)
                .filter(cell -> cell.isVisible() && !cell.isEmpty() && cell.getIndex() >= 0)
                .mapToInt(ListCell::getIndex)
                .min()
                .orElse(-1);
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
