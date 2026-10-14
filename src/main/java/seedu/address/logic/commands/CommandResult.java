package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    /**
     * Which records the main list should show after this command.
     * {@code UNCHANGED} leaves the current list as it is.
     */
    public enum DisplayedRecords {
        UNCHANGED, CUSTOMERS, ALL
    }

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Which records to show in the main list. */
    private final DisplayedRecords displayedRecords;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit, DisplayedRecords displayedRecords) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.displayedRecords = requireNonNull(displayedRecords);
    }

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     * The displayed records are left unchanged.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, DisplayedRecords.UNCHANGED);
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Constructs a {@code CommandResult} that shows {@code displayedRecords} in the main list.
     */
    public CommandResult(String feedbackToUser, DisplayedRecords displayedRecords) {
        this(feedbackToUser, false, false, displayedRecords);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public DisplayedRecords getDisplayedRecords() {
        return displayedRecords;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && displayedRecords == otherCommandResult.displayedRecords;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, displayedRecords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("displayedRecords", displayedRecords)
                .toString();
    }

}
