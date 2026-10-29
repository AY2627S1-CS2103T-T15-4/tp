package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Regressions for one-level history and save failures after an in-memory change. */
public class LogicManagerHistoryTest {
    private static final String ADD_BUDDY = "adddog n/Buddy b/Poodle s/small";
    private static final String ADD_MAX = "adddog n/Max b/Poodle s/large";

    @TempDir
    public Path temporaryFolder;

    private int nextCase;

    @Test
    public void execute_neutralCommands_preserveUndoAndRedo() throws Exception {
        for (String neutral : List.of("help", "summary", "exit")) {
            Harness harness = new Harness();
            harness.logic.execute(ADD_BUDDY);
            harness.logic.execute(neutral);
            harness.logic.execute("undo");
            assertEquals(List.of(), harness.dogNames());

            harness.logic.execute(neutral);
            harness.logic.execute("redo");
            assertEquals(List.of("Buddy"), harness.dogNames());
        }
    }

    @Test
    public void execute_invalidCommands_preservePendingRedo() throws Exception {
        Harness harness = new Harness();
        harness.logic.execute(ADD_BUDDY);
        harness.logic.execute("undo");

        assertThrows(ParseException.class, () -> harness.logic.execute("notacommand"));
        assertThrows(ParseException.class, () -> harness.logic.execute("redo extra"));
        assertThrows(CommandException.class, () -> harness.logic.execute("deletedog 99"));
        harness.logic.execute("redo");
        assertEquals(List.of("Buddy"), harness.dogNames());
    }

    @Test
    public void execute_failedUndo_keepsRedoMatchingAppliedMemory() throws Exception {
        for (IOException failure : failures()) {
            Harness harness = new Harness();
            harness.logic.execute(ADD_BUDDY);
            harness.storage.failure = failure;

            harness.assertSaveFailure("undo", failure);
            assertEquals(List.of(), harness.dogNames());
            assertEquals(List.of("Buddy"), harness.savedDogNames());
            harness.storage.failure = null;
            harness.logic.execute("redo");
            assertEquals(List.of("Buddy"), harness.dogNames());
            assertEquals(harness.dogNames(), harness.savedDogNames());
        }
    }

    @Test
    public void execute_failedRedo_keepsUndoMatchingAppliedMemory() throws Exception {
        for (IOException failure : failures()) {
            Harness harness = new Harness();
            harness.logic.execute(ADD_BUDDY);
            harness.logic.execute("undo");
            harness.storage.failure = failure;

            harness.assertSaveFailure("redo", failure);
            assertEquals(List.of("Buddy"), harness.dogNames());
            assertEquals(List.of(), harness.savedDogNames());
            harness.storage.failure = null;
            harness.logic.execute("undo");
            assertEquals(List.of(), harness.dogNames());
            assertEquals(harness.dogNames(), harness.savedDogNames());
        }
    }

    @Test
    public void execute_failedNewAdd_invalidatesStaleRedo() throws Exception {
        for (IOException failure : failures()) {
            Harness harness = new Harness();
            harness.logic.execute(ADD_BUDDY);
            harness.logic.execute("undo");
            harness.storage.failure = failure;

            harness.assertSaveFailure(ADD_MAX, failure);
            assertEquals(List.of("Max"), harness.dogNames());
            assertEquals(List.of(), harness.savedDogNames());
            harness.storage.failure = null;
            CommandException noRedo = assertThrows(CommandException.class, () -> harness.logic.execute("redo"));
            assertEquals(RedoCommand.MESSAGE_NO_COMMAND, noRedo.getMessage());
            assertEquals(List.of("Max"), harness.dogNames());
            harness.logic.execute("undo");
            assertEquals(List.of(), harness.dogNames());
            assertEquals(harness.dogNames(), harness.savedDogNames());
        }
    }

    @Test
    public void execute_failedClear_discardsHistory() throws Exception {
        for (IOException failure : failures()) {
            for (boolean pendingRedo : List.of(false, true)) {
                Harness harness = new Harness();
                harness.logic.execute(ADD_BUDDY);
                if (pendingRedo) {
                    harness.logic.execute("undo");
                }
                List<String> savedBeforeClear = harness.savedDogNames();
                harness.storage.failure = failure;

                harness.logic.execute("clear");
                harness.assertSaveFailure("y", failure);
                assertEquals(List.of(), harness.dogNames());
                assertEquals(savedBeforeClear, harness.savedDogNames());
                harness.storage.failure = null;
                CommandException noUndo = assertThrows(CommandException.class, () -> harness.logic.execute("undo"));
                CommandException noRedo = assertThrows(CommandException.class, () -> harness.logic.execute("redo"));
                assertEquals(UndoCommand.MESSAGE_NO_COMMAND, noUndo.getMessage());
                assertEquals(RedoCommand.MESSAGE_NO_COMMAND, noRedo.getMessage());
            }
        }
    }

    @Test
    public void execute_restoredDependentFilters_showOnlyRelatedRecords() throws Exception {
        for (String filter : List.of("searchd n/Buddy", "searchc n/Alice")) {
            Harness afterReset = linkedHarness();
            afterReset.logic.execute(filter);
            afterReset.assertFilteredPair();
            afterReset.logic.execute("reset");
            afterReset.logic.execute("undo");
            afterReset.assertFilteredPair();

            Harness afterRedo = linkedHarness();
            afterRedo.logic.execute("reset");
            afterRedo.logic.execute(filter);
            afterRedo.logic.execute("undo");
            assertEquals(2, afterRedo.model.getFilteredCustomerList().size());
            assertEquals(2, afterRedo.model.getFilteredDogList().size());
            afterRedo.logic.execute("redo");
            afterRedo.assertFilteredPair();
        }
    }

    private Harness linkedHarness() throws Exception {
        Harness harness = new Harness();
        Customer alice = new Customer(1, "Alice", "91234567", "alice@example.com", "98765432");
        Customer bob = new Customer(2, "Bob", "81234567", "bob@example.com", "87654321");
        harness.model.addCustomer(alice);
        harness.model.addCustomer(bob);
        Dog buddy = harness.model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Dog max = harness.model.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("large")));
        harness.model.linkDogs(alice.getId(), List.of(buddy.getId()));
        harness.model.linkDogs(bob.getId(), List.of(max.getId()));
        return harness;
    }

    private static List<IOException> failures() {
        return List.of(new IOException("save failed"), new AccessDeniedException("denied.json"));
    }

    private final class Harness {
        private final ModelManager model = new ModelManager();
        private final FailingStorage storage;
        private final LogicManager logic;

        private Harness() {
            Path addressBookPath = temporaryFolder.resolve("history-" + ++nextCase + ".json");
            storage = new FailingStorage(addressBookPath);
            JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("prefs-" + nextCase));
            logic = new LogicManager(model, new StorageManager(storage, prefs));
        }

        private List<String> dogNames() {
            return model.getDogList().stream().map(dog -> dog.getName().toString()).toList();
        }

        private List<String> savedDogNames() throws Exception {
            return storage.readAddressBook().orElseThrow().getDogList().stream()
                    .map(dog -> dog.getName().toString()).toList();
        }

        private void assertSaveFailure(String command, IOException failure) {
            CommandException thrown = assertThrows(CommandException.class, () -> logic.execute(command));
            String expected = failure instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage())
                    : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());
            assertEquals(expected, thrown.getMessage());
            assertSame(failure, thrown.getCause());
        }

        private void assertFilteredPair() {
            assertEquals(List.of("Alice"), model.getFilteredCustomerList().stream()
                    .map(customer -> customer.getName().toString()).toList());
            assertEquals(List.of("Buddy"), model.getFilteredDogList().stream()
                    .map(dog -> dog.getName().toString()).toList());
            assertEquals(2, model.getAddressBook().getDogList().size());
        }
    }

    private static final class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;

        private FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
