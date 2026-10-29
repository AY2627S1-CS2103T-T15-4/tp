package seedu.address.logic;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.Model.ModelState;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private HistorySnapshot undoSnapshot;
    private HistorySnapshot redoSnapshot;
    private boolean awaitingClearConfirmation;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        requireAllNonNull(model, storage);
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");
        if (awaitingClearConfirmation) {
            return executeClearConfirmation(commandText);
        }

        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof ClearCommand) {
            awaitingClearConfirmation = true;
            return new CommandResult(ClearCommand.MESSAGE_CONFIRMATION);
        }
        if (command instanceof UndoCommand) {
            return executeUndo();
        }
        if (command instanceof RedoCommand) {
            return executeRedo();
        }
        return executeParsedCommand(command, commandText);
    }

    private CommandResult executeClearConfirmation(String commandText) throws CommandException {
        String response = commandText.trim();
        if (response.equalsIgnoreCase("n")) {
            awaitingClearConfirmation = false;
            return new CommandResult(ClearCommand.MESSAGE_CANCELLED);
        }
        if (!response.equalsIgnoreCase("y")) {
            throw new CommandException(ClearCommand.MESSAGE_INVALID_CONFIRMATION);
        }
        awaitingClearConfirmation = false;
        return executeParsedCommand(new ClearCommand(), commandText);
    }

    private CommandResult executeUndo() throws CommandException {
        if (undoSnapshot == null) {
            logger.info("Undo requested when there is no undo snapshot.");
            throw new CommandException(UndoCommand.MESSAGE_NO_COMMAND);
        }
        HistorySnapshot undone = undoSnapshot;
        HistorySnapshot redo = new HistorySnapshot(model.createState(), undone.commandText());
        model.restoreState(undone.state());
        undoSnapshot = null;
        redoSnapshot = redo;
        assert redoSnapshot != null;
        logger.info("Undid the previous command and stored a redo snapshot.");
        saveAddressBook(true);
        return new CommandResult(String.format(UndoCommand.MESSAGE_SUCCESS_FORMAT, undone.commandText()));
    }

    private CommandResult executeRedo() throws CommandException {
        if (redoSnapshot == null) {
            logger.info("Redo requested when there is no redo snapshot.");
            throw new CommandException(RedoCommand.MESSAGE_NO_COMMAND);
        }
        HistorySnapshot redone = redoSnapshot;
        HistorySnapshot undo = new HistorySnapshot(model.createState(), redone.commandText());
        model.restoreState(redone.state());
        undoSnapshot = undo;
        redoSnapshot = null;
        assert undoSnapshot != null;
        logger.info("Redid the previous command and stored an undo snapshot.");
        saveAddressBook(true);
        return new CommandResult(String.format(RedoCommand.MESSAGE_SUCCESS_FORMAT, redone.commandText()));
    }

    /**
     * Runs a parsed command. History is updated from the in-memory result before saving.
     * Help, summary and exit still return their result when saving fails.
     */
    private CommandResult executeParsedCommand(Command command, String commandText) throws CommandException {
        boolean isClear = command instanceof ClearCommand;
        boolean isHistoryNeutral = command instanceof HelpCommand || command instanceof SummaryCommand
                || command instanceof ExitCommand;
        ModelState stateBeforeCommand = isClear || isHistoryNeutral ? null : model.createState();
        CommandResult commandResult = command.execute(model);
        recordHistory(isClear, isHistoryNeutral, stateBeforeCommand, commandText);
        saveAddressBook(!isHistoryNeutral);
        return commandResult;
    }

    private void recordHistory(boolean isClear, boolean isHistoryNeutral, ModelState stateBeforeCommand,
            String commandText) {
        if (isClear) {
            undoSnapshot = null;
            redoSnapshot = null;
            logger.info("Cleared undo and redo history.");
        } else if (!isHistoryNeutral) {
            assert stateBeforeCommand != null : "An undoable command must capture a snapshot.";
            undoSnapshot = new HistorySnapshot(stateBeforeCommand, commandText.trim());
            redoSnapshot = null;
        }
    }

    /**
     * One restorable model snapshot paired with the command text shown in feedback.
     * The two values cannot be stored independently.
     */
    private record HistorySnapshot(ModelState state, String commandText) {
        private HistorySnapshot {
            requireNonNull(state);
            requireNonNull(commandText);
            assert !commandText.isBlank() : "History feedback must name a command.";
        }
    }

    /**
     * Saves the current address book. A required save reports the failure to the caller.
     * History has already been updated from the in-memory state when this is called.
     */
    private void saveAddressBook(boolean required) throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            logger.warning("Saving was denied by file or folder permissions. "
                    + "The in-memory records have not been saved.");
            if (required) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
            }
        } catch (IOException ioe) {
            logger.warning("Saving failed with " + ioe.getClass().getSimpleName()
                    + ". The in-memory records have not been saved.");
            if (required) {
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
        }
    }

    @Override
    public ObservableList<Customer> getFilteredCustomerList() {
        return model.getFilteredCustomerList();
    }

    @Override
    public ObservableList<Dog> getFilteredDogList() {
        return model.getFilteredDogList();
    }

    @Override
    public ObservableList<Dog> getDogList() {
        return model.getDogList();
    }

    @Override
    public ObservableList<Activity> getActivityList() {
        return model.getActivityList();
    }

    @Override
    public ObservableList<Activity> getFilteredActivityList() {
        return model.getFilteredActivityList();
    }

    @Override
    public ObservableList<Enrolment> getEnrolmentList() {
        return model.getAddressBook().getEnrolmentList();
    }

    @Override
    public String getActivityEnrolmentSummary(Activity activity) {
        return model.getActivityEnrolmentSummary(activity);
    }

    @Override
    public javafx.beans.Observable getActivityEnrolmentContextChanges() {
        return model.getActivityEnrolmentContextChanges();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
