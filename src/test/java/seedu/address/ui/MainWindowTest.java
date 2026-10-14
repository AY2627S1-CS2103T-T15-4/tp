package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.ui.JavaFxTestUtil.runOnFxThread;
import static seedu.address.ui.JavaFxTestUtil.startJavaFx;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.SearchDogCommand;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.DogSearchPredicate;
import seedu.address.model.dog.Size;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that the main window observes the model's filtered dog list. */
public class MainWindowTest {
    @TempDir
    private Path tempDirectory;

    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        startJavaFx();
    }

    @Test
    public void searchDog_updatesCardsInMainWindow() throws Exception {
        AddressBook book = new AddressBook();
        book.addCustomer(new Customer(7, "Alex", "91234567", "alex@example.com", "98765432"));
        book.addDogFromStorage(new Dog(42, new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        book.addDogFromStorage(new Dog(43, new DogName("Max"), new Breed("Poodle"), new Size("large")));
        ModelManager model = new ModelManager(book, new UserPrefs());
        Path dataFile = tempDirectory.resolve("addressbook.json");
        Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(tempDirectory.resolve("preferences.json"))));

        runOnFxThread(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, dataFile);
            window.fillInnerParts();
            ListView<Supplier<Region>> cards = listView(window.getRecordListPanel());
            assertEquals(3, cards.getItems().size());

            new SearchDogCommand(new DogSearchPredicate("bud")).execute(model);
            assertEquals(1, cards.getItems().size());
            assertEquals("Buddy", nameOnCard(cards.getItems().get(0).get()));

            new SearchDogCommand(new DogSearchPredicate("missing")).execute(model);
            assertEquals(0, cards.getItems().size());
            stage.hide();
        });
    }

    @SuppressWarnings("unchecked")
    private static ListView<Supplier<Region>> listView(RecordListPanel panel) {
        return (ListView<Supplier<Region>>) panel.getRoot().lookup("#recordListView");
    }

    private static String nameOnCard(Region card) {
        return ((Label) card.lookup("#name")).getText();
    }
}
