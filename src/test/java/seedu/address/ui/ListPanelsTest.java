package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;

/** Tests the customer, dog and activity list panels. */
public class ListPanelsTest {
    private static final Customer CUSTOMER = new Customer(1, "Alex Tan", "91234567",
            "alex@example.com", "98765432");
    private static final Dog DOG = new Dog(2, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
    private static final Activity ACTIVITY = new Activity(3, new ActivityName("Walk"),
            LocalDate.of(2028, 2, 29), LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);

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
    public void activityCard_displaysAllFields() throws Exception {
        runOnFxThread(() -> {
            Region card = new ActivityCard(ACTIVITY).getRoot();
            assertEquals("3. ", label(card, "id"));
            assertEquals("Walk", label(card, "name"));
            assertEquals("Date: 2028-02-29", label(card, "date"));
            assertEquals("Time: 09:00 - 10:00", label(card, "time"));
            assertEquals("Recurrence: WEEKLY", label(card, "recurrence"));
        });
    }

    @Test
    public void activityCard_displaysEnrolmentSummaryWhenProvided() throws Exception {
        runOnFxThread(() -> {
            Region card = new ActivityCard(ACTIVITY, "2. Buddy: 2028-03-01").getRoot();
            assertEquals("2. Buddy: 2028-03-01", label(card, "enrolments"));
            assertEquals("Schedule starts: 2028-02-29", label(card, "date"));
        });
    }

    @Test
    public void activityListPanel_refreshesEnrolmentSummaryWhenRecordsChange() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Activity> activities = FXCollections.observableArrayList(ACTIVITY);
            ObservableList<Enrolment> enrolments = FXCollections.observableArrayList();
            ActivityListPanel panel = new ActivityListPanel(activities, enrolments,
                    activity -> enrolments.isEmpty() ? "" : "2. Buddy: 2028-03-01");
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals("Date: 2028-02-29", label(list.getItems().get(0).get(), "date"));
            enrolments.add(new Enrolment(ACTIVITY.getId(), DOG.getId(), LocalDate.of(2028, 3, 1)));
            assertEquals("2. Buddy: 2028-03-01", label(list.getItems().get(0).get(), "enrolments"));
            enrolments.clear();
            assertEquals("", label(list.getItems().get(0).get(), "enrolments"));
        });
    }

    @Test
    public void customerListPanel_displaysCustomersAndOwnedDogs() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Customer> customers = FXCollections.observableArrayList(CUSTOMER);
            ObservableList<Dog> dogs = FXCollections.observableArrayList(DOG.withOwner(1));
            CustomerListPanel panel = new CustomerListPanel(customers, dogs);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Alex Tan", label(list.getItems().get(0).get(), "name"));
            assertEquals("Dog IDs: 2", label(list.getItems().get(0).get(), "dogIds"));
            show(panel);

            customers.add(new Customer(4, "Beth Tan", "92345678", "beth@example.com", "97654321"));
            assertEquals(2, list.getItems().size());
            dogs.set(0, DOG);
            assertEquals("Dog IDs: None", label(list.getItems().get(0).get(), "dogIds"));
        });
    }

    @Test
    public void dogListPanel_displaysAndRefreshesDogs() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Dog> dogs = FXCollections.observableArrayList(DOG);
            DogListPanel panel = new DogListPanel(dogs);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            show(panel);
            dogs.add(new Dog(4, new DogName("Milo"), new Breed("Poodle"), new Size("Large")));
            assertEquals(2, list.getItems().size());
            assertEquals("Milo", label(list.getItems().get(1).get(), "name"));
            dogs.clear();
            assertTrue(list.getItems().isEmpty());
        });
    }

    @Test
    public void activityListPanel_displaysAndRefreshesActivities() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Activity> activities = FXCollections.observableArrayList(ACTIVITY);
            ActivityListPanel panel = new ActivityListPanel(activities);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Walk", label(list.getItems().get(0).get(), "name"));
            show(panel);
            activities.add(new Activity(4, new ActivityName("Training"), ACTIVITY.getStartDate(),
                    ACTIVITY.getStartTime(), ACTIVITY.getEndTime(), Recurrence.ONCE));
            assertEquals(2, list.getItems().size());
            assertEquals("Training", label(list.getItems().get(1).get(), "name"));
        });
    }

    @Test
    public void todayActivityListPanel_showsOnlyTodaysActivitiesAndRefreshes() throws Exception {
        runOnFxThread(() -> {
            LocalDate today = LocalDate.now();
            Activity todayActivity = new Activity(5, new ActivityName("Today walk"), today,
                    LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
            ObservableList<Activity> activities = javafx.collections.FXCollections.observableArrayList(
                    todayActivity,
                    new Activity(6, new ActivityName("Upcoming"), today.plusDays(1),
                            LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE));
            TodayActivityListPanel panel = new TodayActivityListPanel(activities);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Today walk", label(list.getItems().get(0).get(), "name"));

            activities.add(new Activity(7, new ActivityName("Today training"), today,
                    LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.WEEKLY));
            assertEquals(2, list.getItems().size());
            assertEquals("Today training", label(list.getItems().get(1).get(), "name"));
        });
    }

    @Test
    public void emptyPanels_renderWithoutCards() throws Exception {
        runOnFxThread(() -> {
            CustomerListPanel customers = new CustomerListPanel(FXCollections.observableArrayList(),
                    FXCollections.observableArrayList());
            DogListPanel dogs = new DogListPanel(FXCollections.observableArrayList());
            ActivityListPanel activities = new ActivityListPanel(FXCollections.observableArrayList());

            show(customers);
            show(dogs);
            show(activities);
            assertTrue(listView(customers).getItems().isEmpty());
            assertTrue(listView(dogs).getItems().isEmpty());
            assertTrue(listView(activities).getItems().isEmpty());
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    private static void show(UiPart<Region> panel) {
        Scene scene = new Scene(panel.getRoot(), 450, 600);
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Supplier<Region>> listView(UiPart<Region> panel) {
        return (ListView<Supplier<Region>>) panel.getRoot().lookup("#recordListView");
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
