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
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.Model.ModelState;
import seedu.address.model.activity.Activity;
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

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);

        if (command instanceof UndoCommand) {
            if (undoState == null) {
                throw new CommandException(UndoCommand.MESSAGE_NO_COMMAND);
            }
            ModelState state = undoState;
            String undoneCommand = undoCommandText;
            model.restoreState(state);
            try {
                storage.saveAddressBook(model.getAddressBook());
            } catch (AccessDeniedException e) {
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
            } catch (IOException ioe) {
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
            undoState = null;
            undoCommandText = null;
            return new CommandResult(String.format(UndoCommand.MESSAGE_SUCCESS_FORMAT, undoneCommand));
        }

        ModelState stateBeforeCommand = command instanceof ClearCommand ? null : model.createState();
        commandResult = command.execute(model);

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            logger.warning("Command executed, but saving was denied by file or folder permissions. "
                    + "The in-memory changes have not been saved.");
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            logger.warning("Command executed, but saving failed with " + ioe.getClass().getSimpleName()
                    + ". The in-memory changes have not been saved.");
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        if (command instanceof ClearCommand) {
            undoState = null;
            undoCommandText = null;
        } else {
            undoState = stateBeforeCommand;
            undoCommandText = commandText.trim();
        }
        return commandResult;
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
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
