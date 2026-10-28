package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
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
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ResetDisplayCommand;
import seedu.address.logic.commands.SearchCustomerCommand;
import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.logic.commands.UnlinkCommand;
import seedu.address.logic.commands.exceptions.CommandException;
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
import seedu.address.model.dog.exceptions.LinkException;

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
            assertEquals("Owner ID: None", label(card, "owner"));
        });
    }

    @Test
    public void panel_customerDogIds_includeHiddenDogsAndRefreshAfterLinkAndDelete() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(ALEX);
            book.addDogFromStorage(BUDDY);
            book.addDogFromStorage(new Dog(3, new DogName("Max"), new Breed("Poodle"), new Size("small")));
            ModelManager model = new ModelManager(book, new UserPrefs());
            model.updateFilteredDogList(dog -> false);
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList(),
                    model.getDogList()));
            Region initialCard = list.getItems().get(0).get();
            assertEquals("Dog IDs: None", label(initialCard, "dogIds"));
            var fields = initialCard.lookup("#backupContact").getParent().getChildrenUnmodifiable();
            assertEquals(fields.indexOf(initialCard.lookup("#backupContact")) + 1,
                    fields.indexOf(initialCard.lookup("#dogIds")));
            try {
                model.linkDogs(7, List.of(42, 3));
            } catch (LinkException e) {
                throw new AssertionError(e);
            }
            assertEquals(1, list.getItems().size());
            assertEquals("Dog IDs: 3, 42", label(list.getItems().get(0).get(), "dogIds"));
            model.deleteDog(model.getDogList().get(0));
            assertEquals("Dog IDs: 3", label(list.getItems().get(0).get(), "dogIds"));
            model.deleteDog(model.getDogList().get(0));
            assertEquals("Dog IDs: None", label(list.getItems().get(0).get(), "dogIds"));
        });
    }

    @Test
    public void panel_unlink_updatesBothCardsAndKeepsRecords() throws Exception {
        for (UnlinkCommand command : List.of(new UnlinkCommand(List.of(42)), new UnlinkCommand(7))) {
            runOnFxThread(() -> {
                AddressBook book = new AddressBook();
                book.addCustomer(ALEX);
                book.addDogFromStorage(BUDDY.withOwner(7));
                ModelManager model = new ModelManager(book, new UserPrefs());
                var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList(),
                        model.getDogList()));
                assertEquals("Dog IDs: 42", label(list.getItems().get(0).get(), "dogIds"));
                try {
                    command.execute(model);
                } catch (CommandException e) {
                    throw new AssertionError(e);
                }
                assertEquals(2, list.getItems().size());
                assertEquals("Dog IDs: None", label(list.getItems().get(0).get(), "dogIds"));
                assertEquals("Owner ID: None", label(list.getItems().get(1).get(), "owner"));
            });
        }
    }

    @Test
    public void panel_linkAndCustomerDeletion_refreshesOwnerAndKeepsDogs() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(ALEX);
            book.addDogFromStorage(BUDDY);
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));
            try {
                model.linkDogs(7, List.of(42));
            } catch (LinkException e) {
                throw new AssertionError(e);
            }
            assertEquals("Owner ID: 7", label(list.getItems().get(1).get(), "owner"));
            model.deleteCustomer(ALEX);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals("Owner ID: None", label(list.getItems().get(0).get(), "owner"));
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
            assertEquals("Phone Number: 91234567", label(customer, "phone"));
            assertEquals("Email: alex@example.com", label(customer, "email"));
            assertEquals("Backup Contact: 98765432", label(customer, "backupContact"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));

            new Scene(panel.getRoot(), 450, 600);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertTrue(panel.getRoot().lookupAll("#name").stream()
                    .anyMatch(node -> ((Label) node).getText().equals("Buddy")));
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
    public void panel_searchAndResetCommands_displayMatchingCategoryAndRestoreCards() throws Exception {
        runOnFxThread(() -> {
            AddressBook book = new AddressBook();
            book.addCustomer(ALEX);
            book.addDogFromStorage(BUDDY);
            book.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("small")));
            ModelManager model = new ModelManager(book, new UserPrefs());
            var list = listView(new RecordListPanel(model.getFilteredCustomerList(), model.getFilteredDogList()));

            executeCommand(new SearchDogCommand(new DogSearchPredicate("bud")), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals("42. ", label(list.getItems().get(0).get(), "id"));

            executeCommand(new SearchDogCommand(new DogSearchPredicate("missing")), model);
            assertTrue(list.getItems().isEmpty());
            new ResetDisplayCommand().execute(model);
            assertEquals(3, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));
            assertEquals("Buddy", label(list.getItems().get(1).get(), "name"));
            assertEquals("Max", label(list.getItems().get(2).get(), "name"));

            executeCommand(new SearchDogCommand(new DogSearchPredicate(43)), model);
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

            executeCommand(new SearchDogCommand(new DogSearchPredicate("bud")), model);
            executeCommand(new SearchCustomerCommand(new CustomerSearchPredicate("alex")), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Alex", label(list.getItems().get(0).get(), "name"));
            executeCommand(new SearchCustomerCommand(new CustomerSearchPredicate(42)), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Beth", label(list.getItems().get(0).get(), "name"));
            executeCommand(new SearchCustomerCommand(new CustomerSearchPredicate("missing")), model);
            assertTrue(list.getItems().isEmpty());
            new ResetDisplayCommand().execute(model);
            assertEquals(3, list.getItems().size());
            executeCommand(new SearchDogCommand(new DogSearchPredicate("bud")), model);
            assertEquals(1, list.getItems().size());
            assertEquals("Buddy", label(list.getItems().get(0).get(), "name"));
            assertEquals(book, model.getAddressBook());
        });
    }

    private static String label(Region card, String id) {
        return ((Label) card.lookup("#" + id)).getText();
    }

    private static void executeCommand(Command command, ModelManager model) {
        try {
            command.execute(model);
        } catch (CommandException exception) {
            throw new AssertionError(exception);
        }
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
