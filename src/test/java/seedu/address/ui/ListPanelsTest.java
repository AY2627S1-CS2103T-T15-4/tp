package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.activity.Recurrence;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.model.dog.exceptions.LinkException;
import seedu.address.model.dog.exceptions.UnlinkException;

/** Tests the customer, dog and activity list panels. */
public class ListPanelsTest {
    private static final Customer CUSTOMER = new Customer(1, "Alex Tan", "91234567",
            "alex@example.com", "98765432");
    private static final Dog DOG = new Dog(2, new DogName("Buddy"), new Breed("Poodle"), new Size("Small"));
    private static final Activity ACTIVITY = new Activity(3, new ActivityName("Walk"),
            LocalDate.of(2028, 2, 29), LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.WEEKLY);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T04:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);

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
            Region card = new ActivityCard(ACTIVITY, "Dogs with upcoming bookings: 2, 10").getRoot();
            assertEquals("3. ", label(card, "id"));
            assertEquals("Walk", label(card, "name"));
            assertEquals("Schedule starts: 2028-02-29", label(card, "date"));
            assertEquals("Time: 09:00 - 10:00", label(card, "time"));
            assertEquals("Recurrence: WEEKLY", label(card, "recurrence"));
            assertEquals("Dogs with upcoming bookings: 2, 10", label(card, "dogSummary"));
            Label dogSummary = (Label) card.lookup("#dogSummary");
            assertTrue(dogSummary.isWrapText());
            assertEquals(1, dogSummary.getLineSpacing());
            assertEquals(2, ((VBox) dogSummary.getParent()).getSpacing());
            assertEquals(new Insets(10, 14, 10, 14), ((VBox) dogSummary.getParent()).getPadding());
        });
    }

    @Test
    public void activityCard_manyDogIds_wrapsWithoutClippingInNarrowCard() throws Exception {
        runOnFxThread(() -> {
            String dogIds = String.join(", ", IntStream.rangeClosed(1, 30).mapToObj(String::valueOf).toList());
            Region card = new ActivityCard(ACTIVITY, "Dogs with upcoming bookings: " + dogIds).getRoot();
            Scene scene = new Scene(card, 190, 300);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            Label summary = (Label) card.lookup("#dogSummary");

            assertTrue(summary.isWrapText());
            assertTrue(summary.getHeight() > 20);
            assertTrue(summary.getBoundsInParent().getMaxY() <= card.getBoundsInLocal().getMaxY());
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
            assertEquals("2", label(list.getItems().get(0).get(), "dogIds"));
            show(panel);

            customers.add(new Customer(4, "Beth Tan", "92345678", "beth@example.com", "97654321"));
            assertEquals(2, list.getItems().size());
            dogs.set(0, DOG);
            assertEquals("None", label(list.getItems().get(0).get(), "dogIds"));
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
    public void customerAndDogCopyMenus_refreshAfterLinkAndUnlink() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(CUSTOMER);
            book.addDogFromStorage(DOG);
            ModelManager model = new ModelManager(book, new UserPrefs());
            CustomerListPanel customerPanel = new CustomerListPanel(model.getFilteredCustomerList(),
                    model.getDogList());
            DogListPanel dogPanel = new DogListPanel(model.getFilteredDogList());
            ListView<Supplier<Region>> customers = listView(customerPanel);
            ListView<Supplier<Region>> dogs = listView(dogPanel);

            Region customerCard = customers.getItems().get(0).get();
            Region dogCard = dogs.getItems().get(0).get();
            assertEquals(CardCopyFormatter.formatCustomer(CUSTOMER, List.of()),
                    CardInteractionUtil.getCopyValue(
                            CardInteractionUtil.getCopyMenu(customerCard).getItems().get(4)));
            assertTrue(CardInteractionUtil.getCopyMenu(dogCard).getItems().get(1).isDisable());
            assertEquals(CardCopyFormatter.formatDog(DOG), CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(3)));

            try {
                model.linkDogs(1, List.of(2));
            } catch (LinkException exception) {
                throw new AssertionError(exception);
            }

            customerCard = customers.getItems().get(0).get();
            dogCard = dogs.getItems().get(0).get();
            assertEquals(CardCopyFormatter.formatCustomer(CUSTOMER, List.of(2)),
                    CardInteractionUtil.getCopyValue(
                            CardInteractionUtil.getCopyMenu(customerCard).getItems().get(4)));
            assertFalse(CardInteractionUtil.getCopyMenu(dogCard).getItems().get(1).isDisable());
            assertEquals("1", CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(1)));
            assertEquals(CardCopyFormatter.formatDog(DOG.withOwner(1)), CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(3)));

            model.updateFilteredDogList(dog -> false);
            assertTrue(dogs.getItems().isEmpty());
            customerCard = customers.getItems().get(0).get();
            assertEquals(CardCopyFormatter.formatCustomer(CUSTOMER, List.of(2)),
                    CardInteractionUtil.getCopyValue(
                            CardInteractionUtil.getCopyMenu(customerCard).getItems().get(4)));
            model.updateFilteredDogList(dog -> true);

            try {
                model.unlinkDogs(List.of(2));
            } catch (UnlinkException exception) {
                throw new AssertionError(exception);
            }

            customerCard = customers.getItems().get(0).get();
            dogCard = dogs.getItems().get(0).get();
            assertEquals(CardCopyFormatter.formatCustomer(CUSTOMER, List.of()),
                    CardInteractionUtil.getCopyValue(
                            CardInteractionUtil.getCopyMenu(customerCard).getItems().get(4)));
            assertTrue(CardInteractionUtil.getCopyMenu(dogCard).getItems().get(1).isDisable());
            assertEquals(CardCopyFormatter.formatDog(DOG), CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(3)));

            Customer editedCustomer = new Customer(1, "Alex Tan", "00112233",
                    "Updated.Email@example.com", "00998877");
            Dog editedDog = new Dog(2, new DogName("Buddy-O'Neil"),
                    new Breed("German-Shepherd-Mix"), new Size("Large"));
            model.setCustomer(CUSTOMER, editedCustomer);
            model.setDog(DOG, editedDog);

            customerCard = customers.getItems().get(0).get();
            dogCard = dogs.getItems().get(0).get();
            assertEquals("00112233", CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(customerCard).getItems().get(0)));
            assertEquals("Updated.Email@example.com", CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(customerCard).getItems().get(1)));
            assertEquals(CardCopyFormatter.formatCustomer(editedCustomer, List.of()),
                    CardInteractionUtil.getCopyValue(
                            CardInteractionUtil.getCopyMenu(customerCard).getItems().get(4)));
            assertEquals("German-Shepherd-Mix", CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(0)));
            assertEquals(CardCopyFormatter.formatDog(editedDog), CardInteractionUtil.getCopyValue(
                    CardInteractionUtil.getCopyMenu(dogCard).getItems().get(3)));
        });
    }

    @Test
    public void activityListPanel_displaysAndRefreshesActivities() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Activity> activities = FXCollections.observableArrayList(ACTIVITY);
            ActivityListPanel panel = new ActivityListPanel(activities, FXCollections.observableArrayList(),
                    CLOCK, false);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Walk", label(list.getItems().get(0).get(), "name"));
            assertEquals("Dogs with upcoming bookings: None",
                    label(list.getItems().get(0).get(), "dogSummary"));
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
            Activity todayActivity = new Activity(5, new ActivityName("Today walk"), TODAY,
                    LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE);
            ObservableList<Activity> activities = javafx.collections.FXCollections.observableArrayList(
                    todayActivity,
                    new Activity(6, new ActivityName("Upcoming"), TODAY.plusDays(1),
                            LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE));
            ObservableList<Enrolment> enrolments = FXCollections.observableArrayList(
                    new Enrolment(5, 2, TODAY), new Enrolment(5, 10, TODAY.plusDays(1)));
            TodayActivityListPanel panel = new TodayActivityListPanel(activities, enrolments, CLOCK);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals(1, list.getItems().size());
            assertEquals("Today walk", label(list.getItems().get(0).get(), "name"));
            assertEquals("Dogs enrolled on 2026-10-28: 2", label(list.getItems().get(0).get(), "dogSummary"));

            activities.add(new Activity(7, new ActivityName("Today training"), TODAY,
                    LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.WEEKLY));
            assertEquals(2, list.getItems().size());
            assertEquals("Today training", label(list.getItems().get(1).get(), "name"));
            assertEquals("Dogs enrolled on 2026-10-28: None",
                    label(list.getItems().get(1).get(), "dogSummary"));
        });
    }

    @Test
    public void activityListPanel_summariesUseDatedEnrolmentsForEveryRecurrenceAndRefresh() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Activity> activities = FXCollections.observableArrayList(
                    new Activity(1, new ActivityName("Once"), TODAY,
                            LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE),
                    new Activity(2, new ActivityName("Daily"), TODAY.minusDays(5),
                            LocalTime.of(10, 0), LocalTime.of(11, 0), Recurrence.DAILY),
                    new Activity(3, new ActivityName("Weekly"), TODAY.minusWeeks(1),
                            LocalTime.of(11, 0), LocalTime.of(12, 0), Recurrence.WEEKLY));
            ObservableList<Enrolment> enrolments = FXCollections.observableArrayList(
                    new Enrolment(1, 10, TODAY),
                    new Enrolment(2, 2, TODAY.plusDays(1)),
                    new Enrolment(2, 2, TODAY.plusDays(2)),
                    new Enrolment(3, 7, TODAY.minusDays(1)));
            ActivityListPanel panel = new ActivityListPanel(activities, enrolments, CLOCK, false);
            ListView<Supplier<Region>> list = listView(panel);

            assertEquals("Dogs with upcoming bookings: 10", label(list.getItems().get(0).get(), "dogSummary"));
            assertEquals("Dogs with upcoming bookings: 2", label(list.getItems().get(1).get(), "dogSummary"));
            assertEquals("Dogs with upcoming bookings: None", label(list.getItems().get(2).get(), "dogSummary"));

            enrolments.add(new Enrolment(3, 7, TODAY));
            assertEquals("Dogs with upcoming bookings: 7", label(list.getItems().get(2).get(), "dogSummary"));
            enrolments.removeIf(enrolment -> enrolment.getActivityId() == 3);
            assertEquals("Dogs with upcoming bookings: None", label(list.getItems().get(2).get(), "dogSummary"));

            activities.set(0, new Activity(1, new ActivityName("Renamed"), TODAY,
                    LocalTime.of(9, 0), LocalTime.of(10, 0), Recurrence.ONCE));
            assertEquals("Renamed", label(list.getItems().get(0).get(), "name"));
        });
    }

    @Test
    public void emptyPanels_renderWithoutCards() throws Exception {
        runOnFxThread(() -> {
            CustomerListPanel customers = new CustomerListPanel(FXCollections.observableArrayList(),
                    FXCollections.observableArrayList());
            DogListPanel dogs = new DogListPanel(FXCollections.observableArrayList());
            ActivityListPanel activities = new ActivityListPanel(FXCollections.observableArrayList(),
                    FXCollections.observableArrayList());
            TodayActivityListPanel today = new TodayActivityListPanel(FXCollections.observableArrayList(),
                    FXCollections.observableArrayList(), CLOCK);

            show(customers);
            show(dogs);
            show(activities);
            show(today);
            assertTrue(listView(customers).getItems().isEmpty());
            assertTrue(listView(dogs).getItems().isEmpty());
            assertTrue(listView(activities).getItems().isEmpty());
            assertTrue(listView(today).getItems().isEmpty());
            assertEquals("No customers to display.", ((Label) listView(customers).getPlaceholder()).getText());
            assertEquals("No dogs to display.", ((Label) listView(dogs).getPlaceholder()).getText());
            assertEquals("No activities to display.", ((Label) listView(activities).getPlaceholder()).getText());
            assertEquals("No activities today.", ((Label) listView(today).getPlaceholder()).getText());
            assertTrue(listView(customers).getPlaceholder().getStyleClass().contains("empty-state"));
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
