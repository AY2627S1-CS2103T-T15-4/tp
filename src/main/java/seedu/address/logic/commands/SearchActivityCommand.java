package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.StringJoiner;

import seedu.address.model.Model;
import seedu.address.model.activity.ActivitySearchPredicate;
import seedu.address.model.activity.Recurrence;

/** Searches activities by name, date, time, and/or recurrence. */
public class SearchActivityCommand extends Command {
    public static final String COMMAND_WORD = "searcha";
    private static final String MESSAGE_SUCCESS_PREFIX = "Successfully searched activities by ";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + " [n/NAME] [d/YYYY-MM-DD] [t/HH:mm] [r/RECURRENCE]: Searches activities by activity fields.";

    private final String name;
    private final LocalDate date;
    private final LocalTime time;
    private final Recurrence recurrence;

    /** Creates a command containing any combination of activity filter fields. */
    public SearchActivityCommand(String name, LocalDate date, LocalTime time) {
        this(name, date, time, null);
    }

    /** Creates a command containing any combination of activity filter fields. */
    public SearchActivityCommand(String name, LocalDate date, LocalTime time, Recurrence recurrence) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.recurrence = recurrence;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        ActivitySearchPredicate predicate = new ActivitySearchPredicate(name, date, time, recurrence);
        model.updateFilteredActivityList(predicate);
        model.updateFilteredDogList(dog -> isEnrolledInMatchingActivity(model, dog.getId(), predicate, date));
        model.updateFilteredCustomerList(customer -> model.getDogList().stream()
                .filter(dog -> dog.getOwnerId().orElse(-1) == customer.getId())
                .anyMatch(dog -> isEnrolledInMatchingActivity(model, dog.getId(), predicate, date)));
        return new CommandResult(createSuccessMessage());
    }

    private static boolean isEnrolledInMatchingActivity(Model model, int dogId,
            ActivitySearchPredicate predicate, LocalDate date) {
        if (dogId < 1) {
            throw new IllegalArgumentException("Stored dog IDs must be positive.");
        }
        return model.getAddressBook().getEnrolmentList().stream().anyMatch(enrolment ->
                enrolment.getDogId() == dogId && (date == null || enrolment.getDate().equals(date))
                        && model.getActivityList().stream()
                        .filter(activity -> activity.getId() == enrolment.getActivityId())
                        .anyMatch(predicate));
    }

    private String createSuccessMessage() {
        StringJoiner fields = new StringJoiner(", ");
        if (name != null) {
            fields.add("name: " + name);
        }
        if (time != null) {
            fields.add("time: " + time);
        }
        if (date != null) {
            fields.add("date: " + date);
        }
        if (recurrence != null) {
            fields.add("recurrence: " + formatRecurrence(recurrence));
        }
        return MESSAGE_SUCCESS_PREFIX + fields;
    }

    private static String formatRecurrence(Recurrence recurrence) {
        String value = recurrence.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SearchActivityCommand otherCommand)) {
            return false;
        }
        return Objects.equals(name, otherCommand.name)
                && Objects.equals(date, otherCommand.date)
                && Objects.equals(time, otherCommand.time)
                && recurrence == otherCommand.recurrence;
    }

    @Override
    public String toString() {
        return String.format("SearchActivityCommand{name=%s, date=%s, time=%s, recurrence=%s}",
                name, date, time, recurrence);
    }
}
