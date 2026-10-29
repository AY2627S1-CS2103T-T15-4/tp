package seedu.address.logic;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.Enrolment;
import seedu.address.model.customer.Customer;
import seedu.address.model.dog.Dog;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * A clear command first returns a warning and requests confirmation without changing data or history.
     * While confirmation is pending, input is treated as a case-insensitive Y/N answer,
     * ignoring surrounding whitespace. Y clears the data and undo/redo history; N cancels without changing either.
     * Other input leaves confirmation pending.
     *
     * @param commandText The command or pending clear confirmation answer as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution
     *     or a pending confirmation answer is invalid.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of customers */
    ObservableList<Customer> getFilteredCustomerList();

    // @@author Kenneth-Chia
    /** Returns an unmodifiable observable view of filtered dogs. */
    ObservableList<Dog> getFilteredDogList();

    /** Returns an unmodifiable observable view of all dogs. */
    ObservableList<Dog> getDogList();
    // @@author

    /** Returns an unmodifiable observable view of all activities. */
    ObservableList<Activity> getActivityList();

    /**
     * Returns a live unmodifiable observable view of all dated enrolments.
     *
     * @return all dated enrolments
     */
    ObservableList<Enrolment> getEnrolmentList();

    /** Returns an unmodifiable observable view of filtered activities. */
    ObservableList<Activity> getFilteredActivityList();

    /** Returns the current search-specific enrolment summary for an activity. */
    String getActivityEnrolmentSummary(Activity activity);

    /** Returns an observable signal for changes to the activity-card enrolment context. */
    Observable getActivityEnrolmentContextChanges();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
