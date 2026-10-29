package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteDogCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.ResetDisplayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.dog.Breed;
import seedu.address.model.dog.Dog;
import seedu.address.model.dog.DogName;
import seedu.address.model.dog.Size;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("watchdog.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete dog 9";
        assertCommandException(deleteCommand, DeleteDogCommand.MESSAGE_INVALID_ID);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String resetCommand = ResetDisplayCommand.COMMAND_WORD;
        assertCommandSuccess(resetCommand, String.format(ResetDisplayCommand.MESSAGE_SUCCESS, LocalDate.now()), model);
    }

    @Test
    public void execute_summary_preservesFilteredDataAndSavesExistingRecords() throws Exception {
        model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        model.updateFilteredDogList(unused -> false);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals("Overall: 1 dogs | 0 customers\n"
                + "Activity schedules: 0 total | 0 from today onwards\n"
                + "Today (" + LocalDate.now() + "): 0 activities | 0 distinct dogs enrolled",
                logic.execute("summary").getFeedbackToUser());
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(), model.getFilteredDogList());
        assertEquals(before, new AddressBook(new JsonAddressBookStorage(temporaryFolder.resolve("watchdog.json"))
                .readAddressBook().orElseThrow()));
    }

    @Test
    public void execute_summaryStorageFailure_reportsSaveErrorWithoutChangingRecords() {
        for (IOException exception : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("summary.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw exception;
                }
            };
            logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("summaryPrefs.json"))));
            String expected = exception instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, exception.getMessage())
                    : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, exception.getMessage());
            assertCommandFailure("summary", CommandException.class, expected);
        }
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredCustomerList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredCustomerList().remove(0));
    }

    @Test
    public void getFilteredDogList_searchAndReset_updateExistingObservableView() throws Exception {
        Dog buddy = model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        Dog max = model.addDog(new Dog(new DogName("Max"), new Breed("Poodle"), new Size("small")));
        ObservableList<Dog> displayedDogs = logic.getFilteredDogList();

        assertEquals("Successfully searched dogs by name: bud", logic.execute("searchd n/bud").getFeedbackToUser());
        assertEquals(List.of(buddy), displayedDogs);
        assertThrows(UnsupportedOperationException.class, () -> displayedDogs.remove(0));
        assertThrows(ParseException.class, () -> logic.execute("searchd i/-1"));
        assertEquals(List.of(buddy), displayedDogs);

        logic.execute("reset");
        assertEquals(List.of(buddy, max), displayedDogs);
        assertEquals(List.of(buddy, max), logic.getDogList());
    }

    @Test
    public void getDogList_executeAddDog_updatesExistingObservableView() throws Exception {
        ObservableList<Dog> dogs = logic.getDogList();
        int[] notifications = {0};
        dogs.addListener((ListChangeListener<Dog>) change -> notifications[0]++);

        logic.execute("adddog n/Buddy b/Poodle s/small");

        assertEquals(List.of(new Dog(1, new DogName("Buddy"), new Breed("Poodle"), new Size("small"))), dogs);
        assertEquals(1, notifications[0]);
        assertThrows(UnsupportedOperationException.class, () -> dogs.remove(0));
    }

    @Test
    public void execute_undoRedo_restoresDataAndAllowsAnotherUndo() throws Exception {
        ObservableList<Dog> dogs = logic.getDogList();
        String addCommand = "  adddog n/Buddy b/Poodle s/small  ";

        logic.execute(addCommand);
        Dog buddy = dogs.get(0);
        assertEquals(String.format(UndoCommand.MESSAGE_SUCCESS_FORMAT, addCommand.trim()),
                logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(), dogs);
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);

        assertEquals(String.format(RedoCommand.MESSAGE_SUCCESS_FORMAT, addCommand.trim()),
                logic.execute("redo").getFeedbackToUser());
        assertEquals(List.of(buddy), dogs);
        assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);

        logic.execute("undo");
        assertEquals(List.of(), dogs);
    }

    @Test
    public void executeUndoRedo_withoutHistory_throwsCommandException() {
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);
        assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);
    }

    @Test
    public void execute_commandAfterUndo_discardsRedoHistory() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        logic.execute("undo");
        logic.execute("adddog n/Max b/Poodle s/large");

        assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);
        assertEquals("Max", logic.getDogList().get(0).getName().toString());
    }

    @Test
    public void execute_clear_discardsUndoAndRedoHistory() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        logic.execute("undo");
        logic.execute("clear");
        logic.execute("Y");
        assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);

        logic.execute("adddog n/Max b/Poodle s/large");
        logic.execute("clear");
        logic.execute("y");
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);
    }

    @Test
    public void execute_clear_requestsConfirmationWithoutChangingOrSavingData() throws Exception {
        model.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        model.updateFilteredDogList(unused -> false);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertEquals(ClearCommand.MESSAGE_CONFIRMATION, logic.execute("clear").getFeedbackToUser());

        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(), model.getFilteredDogList());
        assertFalse(Files.exists(temporaryFolder.resolve("watchdog.json")));
    }

    @Test
    public void execute_clearConfirmation_acceptsEitherCaseAndSurroundingWhitespace() throws Exception {
        for (String response : List.of("Y", "y", "  Y  ")) {
            logic.execute("adddog n/Buddy b/Poodle s/small");
            logic.execute("clear");

            assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute(response).getFeedbackToUser());
            assertEquals(new AddressBook(), model.getAddressBook());
            assertEquals(new AddressBook(), new AddressBook(
                    new JsonAddressBookStorage(temporaryFolder.resolve("watchdog.json"))
                            .readAddressBook().orElseThrow()));
            assertParseException("y", MESSAGE_UNKNOWN_COMMAND);
        }
    }

    @Test
    public void execute_clearCancellation_preservesDataAndUndoHistory() throws Exception {
        for (String response : List.of("N", "n", "  n  ")) {
            logic.execute("adddog n/Buddy b/Poodle s/small");
            AddressBook before = new AddressBook(model.getAddressBook());
            logic.execute("clear");

            assertEquals(ClearCommand.MESSAGE_CANCELLED, logic.execute(response).getFeedbackToUser());
            assertEquals(before, model.getAddressBook());
            logic.execute("undo");
            assertEquals(List.of(), logic.getDogList());
        }
    }

    @Test
    public void execute_clearCancellation_preservesRedoHistory() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        logic.execute("undo");
        logic.execute("clear");
        logic.execute("n");

        logic.execute("redo");
        assertEquals("Buddy", logic.getDogList().get(0).getName().toString());
    }

    @Test
    public void execute_invalidClearConfirmation_waitsForExplicitAnswer() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        logic.execute("clear");
        for (String response : List.of("yes", "no", "", " ", "clear", "undo", "adddog n/Max b/Poodle s/large")) {
            assertCommandException(response, ClearCommand.MESSAGE_INVALID_CONFIRMATION);
        }

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("y").getFeedbackToUser());
        assertEquals(new AddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_confirmedClearSaveFailure_discardsHistoryAndConsumesConfirmation() throws Exception {
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            setUp();
            logic.execute("adddog n/Buddy b/Poodle s/small");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("clear.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw failure;
                }
            };
            logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("clearPrefs.json"))));
            String expected = failure instanceof AccessDeniedException
                    ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage())
                    : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());

            assertEquals(ClearCommand.MESSAGE_CONFIRMATION, logic.execute("clear").getFeedbackToUser());
            assertThrows(CommandException.class, expected, () -> logic.execute("y"));
            assertEquals(new AddressBook(), model.getAddressBook());
            assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);
            assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);
            assertParseException("y", MESSAGE_UNKNOWN_COMMAND);
        }
    }

    @Test
    public void execute_failedCommand_preservesUndoHistory() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        assertCommandException("delete dog 9", DeleteDogCommand.MESSAGE_INVALID_ID);

        logic.execute("undo");
        assertEquals(List.of(), logic.getDogList());
    }

    @Test
    public void executeUndoRedo_storageFailure_reportsCause() throws Exception {
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            assertHistoryStorageFailure(failure, true);
            assertHistoryStorageFailure(failure, false);
        }
    }

    @Test
    public void execute_retiredSearch_preservesRecordsFiltersAndRedoHistory() throws Exception {
        logic.execute("adddog n/Buddy b/Poodle s/small");
        logic.execute("undo");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String input : List.of("search n/Buddy", "search id/1", "search dog n/Buddy",
                "search customer n/Alex")) {
            assertParseException(input, MESSAGE_UNKNOWN_COMMAND);
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of(), logic.getFilteredDogList());
        }
        logic.execute("redo");
        Dog buddy = logic.getDogList().get(0);
        logic.execute("searchd i/" + buddy.getId());
        assertEquals(List.of(buddy), logic.getFilteredDogList());
        logic.execute("reset");
        logic.execute("undo");
        assertEquals(List.of(buddy), logic.getDogList());
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);
        logic.execute("redo");
        assertEquals(List.of(buddy), logic.getDogList());
    }

    @Test
    public void execute_help_preservesUndoAndRedoHistory() throws Exception {
        String add = "adddog n/Cookie b/Chihuahua s/Small";
        logic.execute(add);
        Dog cookie = logic.getDogList().get(0);
        for (String help : List.of("help", "help summary", "help adddog", "help delete dog")) {
            logic.execute(help);
        }
        assertEquals(List.of(cookie), logic.getDogList());
        assertEquals(String.format(UndoCommand.MESSAGE_SUCCESS_FORMAT, add),
                logic.execute("undo").getFeedbackToUser());
        assertEquals(List.of(), logic.getDogList());
        for (String help : List.of("help", "help summary", "help adddog", "help delete dog")) {
            logic.execute(help);
        }
        assertEquals(String.format(RedoCommand.MESSAGE_SUCCESS_FORMAT, add),
                logic.execute("redo").getFeedbackToUser());
        assertEquals(List.of(cookie), logic.getDogList());
    }

    @Test
    public void execute_helpOnly_doesNotCreateHistory() throws Exception {
        logic.execute("help adddog");
        logic.execute("help summary");
        assertCommandException("undo", UndoCommand.MESSAGE_NO_COMMAND);
        assertCommandException("redo", RedoCommand.MESSAGE_NO_COMMAND);
    }

    @Test
    public void execute_help_preservesFiltersAndFailedHelpPreservesHistory() throws Exception {
        logic.execute("adddog n/Cookie b/Chihuahua s/Small");
        Dog cookie = logic.getDogList().get(0);
        logic.execute("searchd n/Absent");
        logic.execute("help reset");
        assertEquals(List.of(), logic.getFilteredDogList());
        assertEquals(List.of(cookie), logic.getDogList());
        assertThrows(ParseException.class, () -> logic.execute("help unknown"));
        logic.execute("undo");
        assertEquals(List.of(cookie), logic.getFilteredDogList());
        assertThrows(ParseException.class, () -> logic.execute("help summary today"));
        logic.execute("redo");
        assertEquals(List.of(), logic.getFilteredDogList());
    }

    private void assertHistoryStorageFailure(IOException failure, boolean undo) throws Exception {
        model = new ModelManager();
        boolean[] failSave = {false};
        Path historyPath = temporaryFolder.resolve("history.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(historyPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (failSave[0]) {
                    throw failure;
                }
                super.saveAddressBook(addressBook);
            }
        };
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("historyPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
        logic.execute("adddog n/Buddy b/Poodle s/small");
        if (!undo) {
            logic.execute("undo");
        }

        failSave[0] = true;
        String expectedMessage = failure instanceof AccessDeniedException
                ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, failure.getMessage())
                : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute(undo ? "undo" : "redo"));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = "adddog n/Buddy b/Poodle s/small";
        ModelManager expectedModel = new ModelManager();
        expectedModel.addDog(new Dog(new DogName("Buddy"), new Breed("Poodle"), new Size("small")));
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
