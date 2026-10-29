package seedu.address.logic;

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
    private ModelState undoState;
    private String undoCommandText;
    private ModelState redoState;
    private String redoCommandText;
    private boolean awaitingClearConfirmation;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
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
        if (undoState == null) {
            throw new CommandException(UndoCommand.MESSAGE_NO_COMMAND);
        }
        ModelState state = undoState;
        String undoneCommand = undoCommandText;
        ModelState stateAfterUndo = model.createState();
        model.restoreState(state);
        undoState = null;
        undoCommandText = null;
        redoState = stateAfterUndo;
        redoCommandText = undoneCommand;
        saveAddressBook(true);
        return new CommandResult(String.format(UndoCommand.MESSAGE_SUCCESS_FORMAT, undoneCommand));
    }

    private CommandResult executeRedo() throws CommandException {
        if (redoState == null) {
            throw new CommandException(RedoCommand.MESSAGE_NO_COMMAND);
        }
        ModelState stateBeforeRedo = model.createState();
        ModelState state = redoState;
        String redoneCommand = redoCommandText;
        model.restoreState(state);
        undoState = stateBeforeRedo;
        undoCommandText = redoneCommand;
        redoState = null;
        redoCommandText = null;
        saveAddressBook(true);
        return new CommandResult(String.format(RedoCommand.MESSAGE_SUCCESS_FORMAT, redoneCommand));
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
            undoState = null;
            undoCommandText = null;
            redoState = null;
            redoCommandText = null;
        } else if (!isHistoryNeutral) {
            undoState = stateBeforeCommand;
            undoCommandText = commandText.trim();
            redoState = null;
            redoCommandText = null;
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
