// @@author lilosy
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RECURRENCE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;
import seedu.address.model.activity.ActivityName;
import seedu.address.model.activity.Recurrence;

/** Edits an activity series identified by its persistent ID. */
public class EditActivityCommand extends Command {
    public static final String COMMAND_WORD = "editactivity";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the activity identified by its ID. "
            + "Parameters: ID [" + PREFIX_NAME + "NAME] [" + PREFIX_DATE + "YYYY-MM-DD] ["
            + PREFIX_START + "HH:mm] [" + PREFIX_END + "HH:mm] ["
            + PREFIX_RECURRENCE + "once|daily|weekly]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DATE + "2026-11-04 " + PREFIX_START + "10:00";
    public static final String MESSAGE_EDIT_ACTIVITY_SUCCESS = "Edited activity ID %d.";
    public static final String MESSAGE_ACTIVITY_HAS_ENROLMENTS =
            "Activity ID %d has enrolments and cannot be edited. Create a new activity instead.";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_INVALID_ACTIVITY_ID = "The activity ID provided is invalid.";

    private static final Logger logger = LogsCenter.getLogger(EditActivityCommand.class);

    private final int activityId;
    private final EditActivityDescriptor editActivityDescriptor;

    /** Creates a command to edit the activity with {@code activityId}. */
    public EditActivityCommand(int activityId, EditActivityDescriptor editActivityDescriptor) {
        if (activityId <= 0) {
            throw new IllegalArgumentException("Activity ID must be positive.");
        }
        requireNonNull(editActivityDescriptor);
        this.activityId = activityId;
        this.editActivityDescriptor = new EditActivityDescriptor(editActivityDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!editActivityDescriptor.isAnyFieldEdited()) {
            logger.warning("No fields provided to edit activity ID " + activityId);
            throw new CommandException(MESSAGE_NOT_EDITED);
        }

        Activity activityToEdit = model.getActivityList().stream()
                .filter(activity -> activity.getId() == activityId)
                .findFirst()
                .orElseThrow(() -> {
                    logger.warning("No activity found with ID " + activityId);
                    return new CommandException(MESSAGE_INVALID_ACTIVITY_ID);
                });

        try {
            Activity editedActivity = createEditedActivity(activityToEdit, editActivityDescriptor);
            model.setActivity(activityToEdit, editedActivity);
        } catch (IllegalArgumentException e) {
            logger.warning("Could not edit activity ID " + activityId + ": " + e.getMessage());
            throw new CommandException(e.getMessage(), e);
        }
        logger.info("Edited activity ID " + activityId);
        return new CommandResult(String.format(MESSAGE_EDIT_ACTIVITY_SUCCESS, activityId));
    }

    private static Activity createEditedActivity(Activity activity, EditActivityDescriptor descriptor) {
        return new Activity(activity.getId(),
                descriptor.getName().orElse(activity.getName()),
                descriptor.getDate().orElse(activity.getStartDate()),
                descriptor.getStartTime().orElse(activity.getStartTime()),
                descriptor.getEndTime().orElse(activity.getEndTime()),
                descriptor.getRecurrence().orElse(activity.getRecurrence()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditActivityCommand command
                && activityId == command.activityId
                && editActivityDescriptor.equals(command.editActivityDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("activityId", activityId)
                .add("editActivityDescriptor", editActivityDescriptor)
                .toString();
    }

    /** Stores the optional replacement fields for an activity. */
    public static class EditActivityDescriptor {
        private ActivityName name;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private Recurrence recurrence;

        public EditActivityDescriptor() {}

        /** Creates a defensive copy of the descriptor. */
        public EditActivityDescriptor(EditActivityDescriptor toCopy) {
            requireNonNull(toCopy);
            name = toCopy.name;
            date = toCopy.date;
            startTime = toCopy.startTime;
            endTime = toCopy.endTime;
            recurrence = toCopy.recurrence;
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return name != null || date != null || startTime != null || endTime != null || recurrence != null;
        }

        public void setName(ActivityName name) {
            this.name = requireNonNull(name);
        }

        public Optional<ActivityName> getName() {
            return Optional.ofNullable(name);
        }

        public void setDate(LocalDate date) {
            this.date = requireNonNull(date);
        }

        public Optional<LocalDate> getDate() {
            return Optional.ofNullable(date);
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = requireNonNull(startTime);
        }

        public Optional<LocalTime> getStartTime() {
            return Optional.ofNullable(startTime);
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = requireNonNull(endTime);
        }

        public Optional<LocalTime> getEndTime() {
            return Optional.ofNullable(endTime);
        }

        public void setRecurrence(Recurrence recurrence) {
            this.recurrence = requireNonNull(recurrence);
        }

        public Optional<Recurrence> getRecurrence() {
            return Optional.ofNullable(recurrence);
        }

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof EditActivityDescriptor descriptor
                    && Objects.equals(name, descriptor.name)
                    && Objects.equals(date, descriptor.date)
                    && Objects.equals(startTime, descriptor.startTime)
                    && Objects.equals(endTime, descriptor.endTime)
                    && recurrence == descriptor.recurrence;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("date", date)
                    .add("startTime", startTime)
                    .add("endTime", endTime)
                    .add("recurrence", recurrence)
                    .toString();
        }
    }
}
