package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListDogCommand;
import seedu.address.logic.commands.SearchCommand;
import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.customer.CustomerSearchPredicate;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;

public class RecordListPanelTest {
    private static final Dog BUDDY = new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small"));
    private static final Customer ALEX = new Customer(7, "Alex", "91234567", "alex@example.com", "98765432");

    @BeforeAll
    public static void startJavaFx() throws Exception {
        // JavaFX controls need a display. Linux CI can run these tests under xvfb-run.
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
    public void dogCard_displaysPersistentIdAndAllFields() throws Exception {
        runOnFxThread(() -> {
            Region card = new DogCard(BUDDY).getRoot();
            assertEquals("42. ", label(card, "id"));
            assertEquals("Buddy", label(card, "name"));
            assertEquals("Breed: Poodle", label(card, "breed"));
            assertEquals("Size: small", label(card, "size"));
        });
    }

    @Test
    public void panel_mixedRecords_rendersCustomersBeforeDogs() throws Exception {
        runOnFxThread(() -> {
            RecordListPanel panel = new RecordListPanel(FXCollections.observableArrayList(ALEX),
                    FXCollections.observableArrayList(BUDDY));
            ListView<Supplier<Region>> list = listView(panel);
            assertEquals(2, list.getItems().size());
            Region customer = list.getItems().get(0).get();
            assertEquals("Alex", label(customer, "name"));
            assertEquals("91234567", label(customer, "phone"));
            assertEquals("alex@example.com", label(customer, "email"));
            assertEquals("98765432", label(customer, "backupContact"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));

            new Scene(panel.getRoot(), 450, 600);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertTrue(panel.getRoot().lookupAll("#name").stream()
                    .anyMatch(node -> ((Label) node).getText().equals("Buddy")));
        });
    }

    @Test
    public void panel_showCustomersOnly_hidesDogsUntilShowAll() throws Exception {
        runOnFxThread(() -> {
            var customers = FXCollections.observableArrayList(ALEX);
            var dogs = FXCollections.observableArrayList(BUDDY);
            RecordListPanel panel = new RecordListPanel(customers, dogs);
            panel.showCustomersOnly();
            ListView<Supplier<Region>> list = listView(panel);
            assertEquals(1, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));

            dogs.add(new Dog(43, new DogName("Coco"), new Breed("Poodle"), new Size("small")));
            assertEquals(1, list.getItems().size());

            panel.showAll();
            assertEquals(3, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));
            assertEquals("Coco", label(list.getItems().get(2).get(), "name"));
        });
    }

    @Test
    public void panel_sourceChangesAndCustomerFilter_updateCards() throws Exception {
        runOnFxThread(() -> {
            var customers = FXCollections.<Customer>observableArrayList();
            var filteredCustomers = new FilteredList<>(customers);
            var dogs = FXCollections.<Dog>observableArrayList();
            var list = listView(new RecordListPanel(filteredCustomers, dogs));
            assertTrue(list.getItems().isEmpty());

            customers.add(ALEX);
            dogs.add(BUDDY);
            assertEquals(2, list.getItems().size());
            filteredCustomers.setPredicate(customer -> false);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));

            dogs.set(0, new Dog(42, new DogName("Buddy Updated"), new Breed("Poodle"), new Size("large")));
            assertEquals("Buddy Updated", label(list.getItems().get(0).get(), "name"));
            assertEquals("Size: large", label(list.getItems().get(0).get(), "size"));
            filteredCustomers.setPredicate(customer -> true);
            assertEquals(2, list.getItems().size());
            dogs.clear();
            assertEquals(1, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));
            customers.clear();
            assertTrue(list.getItems().isEmpty());
        });
    }

    @Test
    public void panel_searchAndListCommands_displayMatchingCategoryAndRestoreCards() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(ALEX);
            book.addDogFromStorage(BUDDY);
            book.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("small")));
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));

            new SearchDogCommand(new DogSearchPredicate("bud")).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals("42. ", label(list.getItems().get(0).get(), "id"));

            new SearchDogCommand(new DogSearchPredicate("missing")).execute(model);
            assertTrue(list.getItems().isEmpty());
            new ListDogCommand().execute(model);
            assertEquals(2, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals("Max", label(list.getItems().get(1).get(), "name"));

            new SearchDogCommand(new DogSearchPredicate(43)).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Max", label(list.getItems().get(0).get(), "name"));
            assertEquals(book, model.getAddressBook());
        });
    }

    @Test
    public void panel_customerSearch_switchesBetweenCategoriesAndRestoresCustomers() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(ALEX);
            book.addCustomer(new Customer(42, "Beth", "92345678", "beth@example.com", "97654321"));
            book.addDogFromStorage(BUDDY);
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));

            new SearchDogCommand(new DogSearchPredicate("bud")).execute(model);
            new SearchCustomerCommand(new CustomerSearchPredicate("alex")).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));
            new SearchCustomerCommand(new CustomerSearchPredicate(42)).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Beth", label(list.getItems().get(0).get(), "name"));
            new SearchCustomerCommand(new CustomerSearchPredicate("missing")).execute(model);
            assertTrue(list.getItems().isEmpty());
            new ListCommand().execute(model);
            assertEquals(2, list.getItems().size());
            new ListDogCommand().execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals(book, model.getAddressBook());
        });
    }

    @Test
    public void panel_globalSearch_showsBothCategoriesAndSwitchesToSpecificSearch() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(new Customer(42, "Buddy Tan", "91234567", "buddy@example.com", "98765432"));
            book.addDogFromStorage(BUDDY);
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));

            new SearchCommand("bud").execute(model);
            assertEquals(2, list.getItems().size());
            assertEquals("Buddy Tan", label(list.getItems().get(0).get(), "name"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));
            new SearchCustomerCommand(new CustomerSearchPredicate("bud")).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy Tan", label(list.getItems().get(0).get(), "name"));
            new SearchCommand(42).execute(model);
            assertEquals(2, list.getItems().size());
            new SearchDogCommand(new DogSearchPredicate(42)).execute(model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            new SearchCommand("missing").execute(model);
            assertTrue(list.getItems().isEmpty());
            assertEquals(book, model.getAddressBook());
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Supplier<Region>> listView(RecordListPanel panel) {
        return (ListView<Supplier<Region>>) panel.getRoot().lookup("#recordListView");
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
